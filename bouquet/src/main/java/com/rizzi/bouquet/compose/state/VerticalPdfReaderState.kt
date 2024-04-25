package com.rizzi.bouquet.compose.state

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.center
import androidx.compose.ui.util.fastLastOrNull
import androidx.core.net.toUri
import com.rizzi.bouquet.DocumentResource
import com.rizzi.bouquet.loader.DocumentRequest

val LazyListLayoutInfo.viewportCenterYPx: Float
    get() = viewportSize.center.y.toFloat()

fun VerticalPdfReaderState.absoluteViewportCenterY(): Float {
    return lazyState.layoutInfo.viewportCenterYPx
}

fun VerticalPdfReaderState.relativeViewportCenterY(): Float {
    val middleOfTheScreen = lazyState.layoutInfo.viewportCenterYPx
    val finalMiddleOfTheScreen = middleOfTheScreen - (zoomState.offsetY / zoomState.scale)

    return finalMiddleOfTheScreen
}

class VerticalPdfReaderState(
    request: DocumentRequest,
    zoomEnabled: Boolean = false,
    isAccessibleEnable: Boolean = false,
) : PdfReaderState(request, zoomEnabled, isAccessibleEnable) {

    var lazyState: LazyListState = LazyListState()
        private set

    override val currentPage: Int
        get() = currentPage2()

    override val isScrolling: Boolean
        get() = lazyState.isScrollInProgress

    private fun currentPage2(): Int {
        return renderer?.let { pdfRender ->
            with(lazyState) {
                val finalMiddleOfTheScreen = relativeViewportCenterY()
                layoutInfo.visibleItemsInfo
                    .takeIf { it.isNotEmpty() }
                    ?.fastLastOrNull { it.offset <= finalMiddleOfTheScreen }
                    ?.index?.plus(1)
            }
        } ?: 0
    }

    private fun currentPage(): Int {
        return renderer?.let { pdfRender ->
            val currentMinIndex = lazyState.firstVisibleItemIndex
            var lastVisibleIndex = currentMinIndex

            val minIndexHeight = pdfRender.pageLists[currentMinIndex].dimension.height * zoomState.scale
            var totalVisiblePortion = minIndexHeight - lazyState.firstVisibleItemScrollOffset

            for (i in currentMinIndex + 1 until pageCount) {
                val pageHeight = pdfRender.pageLists[i].dimension.height * zoomState.scale

                val newTotalVisiblePortion = totalVisiblePortion + pageHeight
                if (newTotalVisiblePortion <= pdfRender.viewportSize.height) {
                    lastVisibleIndex = i
                    totalVisiblePortion = newTotalVisiblePortion
                } else {
                    break
                }
            }
            lastVisibleIndex + 1
        } ?: 0
    }

    companion object {
        val Saver: Saver<VerticalPdfReaderState, *> = listSaver(
            save = {
                val resource = it.file
                    ?.let { file -> DocumentResource.Local(file.toUri()) }
                    ?: it.request.type

                listOf(
                    resource,
                    it.zoomEnabled,
                    it.isAccessibleEnable,
                    it.lazyState.firstVisibleItemIndex,
                    it.lazyState.firstVisibleItemScrollOffset
                )
            },
            restore = {
                VerticalPdfReaderState(
                    DocumentRequest(it[0] as DocumentResource),
                    it[1] as Boolean,
                    it[2] as Boolean
                ).apply {
                    lazyState = LazyListState(
                        firstVisibleItemIndex = it[3] as Int,
                        firstVisibleItemScrollOffset = it[4] as Int
                    )
                }
            }
        )
    }
}

@Composable
fun rememberVerticalPdfReaderState(
    request: DocumentRequest,
    zoomEnabled: Boolean = true,
    accessibleEnabled: Boolean = false,
): VerticalPdfReaderState {
    return rememberSaveable(saver = VerticalPdfReaderState.Saver) {
        VerticalPdfReaderState(request, zoomEnabled, accessibleEnabled)
    }
}
