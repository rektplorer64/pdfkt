package com.rizzi.bouquet

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PdfDocumentRenderer internal constructor(
    private val fileDescriptor: ParcelFileDescriptor,
    private val textForEachPage: List<String>,
    val viewportSize: IntSize,
    val orientation: Orientation,
) {
    private val pdfRenderer = PdfRenderer(fileDescriptor)

    val pageCount: Int
        get() = pdfRenderer.pageCount

    private val mutex: Mutex = Mutex()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    internal val pageLists: List<Page> = List(pdfRenderer.pageCount) {
        Page(
            mutex = mutex,
            index = it,
            textForPage = textForEachPage.getOrElse(it) { "" },
            pdfRenderer = pdfRenderer,
            coroutineScope = coroutineScope,
            width = viewportSize.width,
            height = viewportSize.height,
            orientation = orientation
        )
    }

    fun close() {
        coroutineScope.launch {
            pageLists.forEach {
                it.job?.cancelAndJoin()
                it.recycle()
            }
            pdfRenderer.close()
            fileDescriptor.close()
        }
    }

    internal class Page internal constructor(
        val mutex: Mutex,
        val index: Int,
        val textForPage: String,
        val pdfRenderer: PdfRenderer,
        val coroutineScope: CoroutineScope,
        width: Int,
        height: Int,
        orientation: Orientation
    ) {
        val dimension = pdfRenderer.openPage(index).use {
            when (orientation) {
                Orientation.Vertical -> {
                    val h = it.height * (width.toFloat() / it.width)
                    val dim = Dimension(
                        height = h.toInt(),
                        width = width
                    )
                    dim
                }
                Orientation.Horizontal -> {
                    val w = it.width * (height.toFloat() / it.height)
                    val dim = Dimension(
                        height = height,
                        width = w.toInt()
                    )
                    dim
                }
            }
        }

        var job: Job? = null

        val stateFlow = MutableStateFlow<PageContent>(
            PageContent.Blank(
                width = dimension.width,
                height = dimension.height
            )
        )

        var isLoaded = false

        fun load() {
            if (!isLoaded) {
                job = coroutineScope.launch {
                    mutex.withLock {
                        val newBitmap: Bitmap
                        pdfRenderer.openPage(index).use { currentPage ->
                            newBitmap = createBlankBitmap(
                                width = dimension.width,
                                height = dimension.height
                            )
                            currentPage.render(
                                newBitmap,
                                null,
                                null,
                                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                            )
                        }
                        isLoaded = true
                        stateFlow.emit(PageContent.Content(newBitmap, textForPage))
                    }
                }
            }
        }

        fun recycle() {
            isLoaded = false
            val oldBitmap = stateFlow.value as? PageContent.Content
            stateFlow.tryEmit(
                PageContent.Blank(
                    width = dimension.width,
                    height = dimension.height
                )
            )
            oldBitmap?.bitmap?.recycle()
        }

        private fun createBlankBitmap(
            width: Int,
            height: Int
        ): Bitmap {
            return createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            ).apply {
                val canvas = Canvas(this)
                canvas.drawColor(android.graphics.Color.WHITE)
                canvas.drawBitmap(this, 0f, 0f, null)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Page

            if (index != other.index) return false
            if (textForPage != other.textForPage) return false
            if (stateFlow != other.stateFlow) return false
            if (isLoaded != other.isLoaded) return false

            return true
        }

        override fun hashCode(): Int {
            var result = index
            result = 31 * result + textForPage.hashCode()
            result = 31 * result + stateFlow.hashCode()
            result = 31 * result + isLoaded.hashCode()
            return result
        }

        data class Dimension(
            val height: Int,
            val width: Int
        )


    }
}

@Composable
internal fun PageRecyclingEffects(renderer: PdfDocumentRenderer, pageIndex: Int) {
    DisposableEffect(Unit) {
        val page = renderer.pageLists[pageIndex]
        page.load()
        onDispose { page.recycle() }
    }
}

sealed interface PageContent {
    data class Content(
        val bitmap: Bitmap,
        val contentDescription: String
    ) : PageContent

    data class Blank(
        val width: Int,
        val height: Int
    ) : PageContent
}
