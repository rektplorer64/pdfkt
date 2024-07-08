package com.rizzi.bouquet.compose.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import coil.compose.EqualityDelegate
import com.rizzi.bouquet.PdfDocumentRenderer
import com.rizzi.bouquet.loader.DocumentLoader
import com.rizzi.bouquet.loader.DocumentRequest
import net.engawapg.lib.zoomable.ZoomState
import java.io.File

typealias StatusChangeCallback = (ResultStatus<RenderingComponent>) -> Unit

@Suppress("PropertyName")
abstract class PdfReaderState(
    request: DocumentRequest,
    documentLoader: DocumentLoader,
    zoomEnabled: Boolean = false,
    val accessibilityEnabled: Boolean = false,
    onStatusChange: StatusChangeCallback? = null,
) {

    open val maxScale: Float = 3f

    val zoomState = ZoomState(maxScale = maxScale)

    var documentLoader: DocumentLoader by mutableStateOf(documentLoader)
        internal set

    var request: DocumentRequest by mutableStateOf(request)
        internal set

    var zoomEnabled by mutableStateOf(zoomEnabled)

    var status by mutableStateOf<ResultStatus<RenderingComponent>>(ResultStatus.NotStarted)
        private set

    var onStatusChange: StatusChangeCallback? = onStatusChange
        internal set

    val file: File?
        get() = (status as? ResultStatus.Success)?.data?.file

    val renderer: PdfDocumentRenderer?
        get() = (status as? ResultStatus.Success)?.data?.renderer

    val pageCount: Int
        get() = renderer?.pageCount ?: 0

    internal var refresher by mutableIntStateOf(0)
        private set

    abstract val currentPage: Int

    abstract val isScrolling: Boolean

    internal fun onStatusUpdate(newStatus: ResultStatus<RenderingComponent>) {
        status = newStatus
        onStatusChange?.invoke(newStatus)
    }

    fun refresh() {
        close()
        refresher++
        status = ResultStatus.Loading(progress = 0f)
    }

    fun close() {
        renderer?.close()
        status = ResultStatus.NotStarted
    }
}

data class RenderingComponent(
    val file: File?,
    val renderer: PdfDocumentRenderer
)

sealed class ResultStatus<out T> {
    data object NotStarted : ResultStatus<Nothing>()
    data class Loading(val progress: Float) : ResultStatus<Nothing>()
    data class Success<T>(val data: T) : ResultStatus<T>()
    data class Error<T>(val data: T?, val throwable: Throwable) : ResultStatus<T>()
}

@Stable
internal class PdfReaderStateParams(
    val request: DocumentRequest,
    val requestEqualityDelegate: EqualityDelegate,
    val documentLoader: DocumentLoader
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is PdfReaderStateParams &&
                requestEqualityDelegate.equals(request, other.request) &&
                documentLoader == other.documentLoader
    }

    override fun hashCode(): Int {
        var result = requestEqualityDelegate.hashCode(request)
        result = 31 * result + documentLoader.hashCode()
        return result
    }
}