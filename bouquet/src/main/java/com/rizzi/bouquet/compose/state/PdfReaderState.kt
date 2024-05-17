package com.rizzi.bouquet.compose.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rizzi.bouquet.PdfDocumentRenderer
import com.rizzi.bouquet.loader.DocumentRequest
import net.engawapg.lib.zoomable.ZoomState
import java.io.File

@Suppress("PropertyName")
abstract class PdfReaderState(
    val request: DocumentRequest,
    zoomEnabled: Boolean = false,
    val isAccessibleEnable: Boolean = false,
) {

    open val maxScale: Float = 3f

    val zoomState = ZoomState(maxScale = maxScale)

    var zoomEnabled by mutableStateOf(zoomEnabled)

    var status by mutableStateOf<ResultStatus<RenderingComponent>>(ResultStatus.NotStarted)
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
    object NotStarted : ResultStatus<Nothing>()
    data class Loading(val progress: Float) : ResultStatus<Nothing>()
    data class Success<T>(val data: T) : ResultStatus<T>()
    data class Error<T>(val data: T?, val throwable: Throwable) : ResultStatus<T>()
}