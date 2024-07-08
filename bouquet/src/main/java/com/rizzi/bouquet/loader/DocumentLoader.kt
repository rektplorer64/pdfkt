package com.rizzi.bouquet.loader

import android.content.Context
import android.os.ParcelFileDescriptor
import com.rizzi.bouquet.DocumentResource
import com.rizzi.bouquet.cacheBase64AsPdfFile
import com.rizzi.bouquet.generateFileName
import com.rizzi.bouquet.loader.EventListener.Factory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.toHexString
import java.io.File
import java.io.IOException

interface DocumentLoader {

    suspend fun execute(
        request: DocumentRequest,
        listener: ExecutionListener?
    ): DocumentResult

    fun buildUpon(): Builder

    class Builder {
        private val applicationContext: Context
        private var callFactory: Lazy<Call.Factory>?
        private var eventListenerFactory: Lazy<Factory>?

        constructor(context: Context) {
            applicationContext = context.applicationContext
            callFactory = null
            eventListenerFactory = null
        }

        fun okHttpClient(initializer: () -> OkHttpClient) = callFactory(initializer)

        fun callFactory(callFactory: Call.Factory) = apply {
            this.callFactory = lazyOf(callFactory)
        }

        fun callFactory(initializer: () -> Call.Factory) = apply {
            this.callFactory = lazy(initializer)
        }

        fun eventListener(listener: EventListener) = apply {
            this.eventListenerFactory = lazyOf(
                Factory { listener }
            )
        }

        fun eventListener(factory: Factory) = apply {
            this.eventListenerFactory = lazyOf(factory)
        }

        fun build(): DocumentLoader {
            return RealDocumentLoader(
                applicationContext = applicationContext,
                callFactoryLazy = callFactory,
                eventListenerFactory = eventListenerFactory
            )
        }
    }
}

class RealDocumentLoader(
    val applicationContext: Context,
    val callFactoryLazy: Lazy<Call.Factory>?,
    val eventListenerFactory: Lazy<Factory>?
) : DocumentLoader {

    companion object {
        private val DefaultCallFactory by lazy {
            OkHttpClient.Builder().build()
        }
    }

    override suspend fun execute(
        request: DocumentRequest,
        listener: ExecutionListener?
    ): DocumentResult =
        withContext(Dispatchers.IO) {
            val eventListener =
                (eventListenerFactory?.value ?: Factory.Empty).create(request)

            eventListener.onStart(request)
            listener?.onStart()

            runCatching {
                when (val res = request.type) {
                    is DocumentResource.Local -> {
                        requireNotNull(res.uri) {
                            "Local file URI cannot be null"
                        }

                        val descriptor = applicationContext.contentResolver
                            .openFileDescriptor(res.uri, "r")
                            ?: throw IOException("File not found")

                        null to descriptor
                    }

                    is DocumentResource.Asset -> {
                        val bufferSize = 8192
                        val inputStream = applicationContext.resources.openRawResource(res.assetId)
                        val outFile = File(applicationContext.cacheDir, generateFileName())

                        inputStream.use { input ->
                            outFile.outputStream().use { output ->
                                val totalBytes = input.available()

                                var data = ByteArray(bufferSize)
                                var count = input.read(data)

                                var progress = count
                                while (count != -1) {
                                    output.write(data, 0, count)
                                    data = ByteArray(bufferSize)
                                    count = input.read(data)

                                    progress += count

                                    val progressFinal = progress * (100 / totalBytes.toFloat())
                                    eventListener.onLoading(request, progressFinal)
                                    listener?.onLoading(progressFinal)
                                }
                            }
                        }

                        outFile to ParcelFileDescriptor.open(
                            outFile,
                            ParcelFileDescriptor.MODE_READ_ONLY
                        )
                    }

                    is DocumentResource.Base64 -> {
                        val file = applicationContext.cacheBase64AsPdfFile(
                            base64 = res.file,
                            cacheFileName = generateFileName()
                        )

                        file to ParcelFileDescriptor.open(
                            file,
                            ParcelFileDescriptor.MODE_READ_ONLY
                        )
                    }

                    is DocumentResource.Remote -> {
                        val bufferSize = 8192
                        var downloaded = 0

                        val file = File(
                            applicationContext.cacheDir,
                            generateFileName(res.url.hashCode().toHexString())
                        )

                        val hasCache = file.isFile && file.length() > 0
                        if (!hasCache) {
                            val response = (callFactoryLazy?.value ?: DefaultCallFactory).newCall(
                                Request.Builder()
                                    .get()
                                    .url(res.url)
                                    .headers(
                                        Headers.headersOf(
                                            *res.headers
                                                .flatMap { listOf(it.key, it.value) }
                                                .toTypedArray()
                                        )
                                    )
                                    .build()
                            ).execute().body
                                ?: throw IllegalStateException("Response body cannot be empty...")

                            val byteStream = response.byteStream()

                            byteStream.use { input ->
                                file.outputStream().use { output ->
                                    val totalBytes = response.contentLength()
                                    var data = ByteArray(bufferSize)
                                    var count = input.read(data)
                                    while (count != -1) {
                                        if (totalBytes > 0) {
                                            downloaded += bufferSize

                                            val progressFinal = downloaded * (100 / totalBytes.toFloat())
                                            eventListener.onLoading(request, progressFinal)
                                            listener?.onLoading(progressFinal)
                                        }
                                        output.write(data, 0, count)
                                        data = ByteArray(bufferSize)
                                        count = input.read(data)
                                    }
                                }
                            }
                        }

                        file to ParcelFileDescriptor.open(
                            file,
                            ParcelFileDescriptor.MODE_READ_ONLY
                        )
//                    val textForEachPage = if (state.isAccessibleEnable) {
//                        getTextByPage(applicationContext, fileDescriptor)
//                    } else emptyList()

//                    state.pdfRender = BouquetPdfRender(
//                        fileDescriptor = fileDescriptor,
//                        textForEachPage = textForEachPage,
//                        viewportSize = viewportSize,
//                        orientation = orientation
//                    )
//                    state.mFile = file
                    }
                }
            }
                .onFailure {
                    val result =  DocumentResult.Error(
                        request = request,
                        throwable = it
                    )

                    if (it is CancellationException) {
                        eventListener.onCancel(request)
                        listener?.onCancel()
                    } else {
                        eventListener.onFailure(request, it)
                        listener?.onFailure(result)
                    }

                    return@withContext result
                }
                .onSuccess { (file, descriptor) ->
                    val result = DocumentResult.Success(
                        request = request,
                        file = file,
                        fileDescriptor = descriptor
                    )
                    eventListener.onSuccess(request)
                    listener?.onSuccess(result)

                    return@withContext result
                }

            return@withContext DocumentResult.Error(
                request = request,
                throwable = error("Unreachable")
            ).also { err ->
                listener?.onFailure(err)
            }
        }

    override fun buildUpon(): DocumentLoader.Builder {
        return DocumentLoader.Builder(applicationContext).apply {
            callFactoryLazy?.value?.let {
                callFactory(it)
            }
            eventListenerFactory?.value?.let {
                eventListener(it)
            }
        }
    }
}

interface EventListener {
    fun onStart(request: DocumentRequest)
    fun onLoading(request: DocumentRequest, progress: Float)
    fun onSuccess(request: DocumentRequest)
    fun onCancel(request: DocumentRequest)
    fun onFailure(request: DocumentRequest, throwable: Throwable)

    fun interface Factory {
        fun create(request: DocumentRequest): EventListener

        companion object {
            val Empty = Factory { EventListener.Empty }
        }
    }

    companion object {
        val Empty = object : EventListener {
            override fun onStart(request: DocumentRequest) {
            }

            override fun onLoading(request: DocumentRequest, progress: Float) {
            }

            override fun onSuccess(request: DocumentRequest) {
            }

            override fun onCancel(request: DocumentRequest) {
            }

            override fun onFailure(request: DocumentRequest, throwable: Throwable) {
            }
        }
    }
}