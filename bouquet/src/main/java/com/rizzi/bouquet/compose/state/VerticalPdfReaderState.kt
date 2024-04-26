package com.rizzi.bouquet.compose.state

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.center
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastLastOrNull
import androidx.core.net.toUri
import com.rizzi.bouquet.DocumentResource
import com.rizzi.bouquet.compose.DOCUMENT_PAGE_CONTENT_TYPE
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
        get() = currentPage()

    override val isScrolling: Boolean
        get() = lazyState.isScrollInProgress

    private fun currentPage(): Int {
        return renderer?.let { pdfRender ->
            with(lazyState) {
                val finalMiddleOfTheScreen = relativeViewportCenterY()

                val currentPageVisibleItem = layoutInfo.visibleItemsInfo
                    .fastFilter { it.contentType == DOCUMENT_PAGE_CONTENT_TYPE }
                    .takeIf { it.isNotEmpty() }
                    ?.fastLastOrNull { it.offset <= finalMiddleOfTheScreen }

                // We can treat key as pageNo because it is specified as LazyReaderListScope.pages and LazyReaderListScope.pagesIndexed
                val pageNumber = (currentPageVisibleItem?.key as? Int) ?: currentPageVisibleItem?.index
                pageNumber?.plus(1)
            }
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
