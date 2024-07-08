package com.rizzi.bouquet.compose.state

import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.center
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastLastOrNull
import coil.compose.DefaultModelEqualityDelegate
import coil.compose.EqualityDelegate
import com.rizzi.bouquet.compose.DOCUMENT_PAGE_CONTENT_TYPE
import com.rizzi.bouquet.loader.DocumentLoader
import com.rizzi.bouquet.loader.DocumentRequest

internal val LazyListLayoutInfo.viewportCenterYPx: Float
    get() = viewportSize.center.y.toFloat()

internal fun LazyListPdfReaderState.absoluteViewportCenterY(): Float {
    return lazyState.layoutInfo.viewportCenterYPx
}

internal fun LazyListPdfReaderState.relativeViewportCenterY(): Float {
    val middleOfTheScreen = lazyState.layoutInfo.viewportCenterYPx
    val finalMiddleOfTheScreen = middleOfTheScreen - (zoomState.offsetY / zoomState.scale)

    return finalMiddleOfTheScreen
}

class LazyListPdfReaderState(
    request: DocumentRequest,
    documentLoader: DocumentLoader,
    lazyListState: LazyListState = LazyListState(),
    zoomEnabled: Boolean = false,
    accessibilityEnabled: Boolean = false,
    onStatusChange: StatusChangeCallback? = null,
) : PdfReaderState(
    request = request,
    documentLoader = documentLoader,
    zoomEnabled = zoomEnabled,
    accessibilityEnabled = accessibilityEnabled,
    onStatusChange = onStatusChange
) {

    var lazyState: LazyListState = lazyListState
        private set

    override val currentPage: Int
        get() = currentPage()

    override val isScrolling: Boolean
        get() = lazyState.isScrollInProgress

    private fun currentPage(): Int {
        return renderer?.let { _ ->
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
}

@Composable
fun rememberLazyListPdfReaderState(
    request: DocumentRequest,
    lazyListState: LazyListState = rememberLazyListState(),
    documentLoader: DocumentLoader = run {
        val context = LocalContext.current
        remember { DocumentLoader.Builder(context).build() }
    },
    zoomEnabled: Boolean = true,
    accessibleEnabled: Boolean = false,
    onStatusChange: StatusChangeCallback? = null,
    requestEqualityDelegate: EqualityDelegate = DefaultModelEqualityDelegate,
): LazyListPdfReaderState {
    val params = PdfReaderStateParams(
        request = request,
        requestEqualityDelegate = requestEqualityDelegate,
        documentLoader = documentLoader
    )

    return remember {
        LazyListPdfReaderState(
            request = request,
            documentLoader = params.documentLoader,
            lazyListState = lazyListState,
            zoomEnabled = zoomEnabled,
            accessibilityEnabled = accessibleEnabled,
            onStatusChange = onStatusChange,
        )
    }.apply {
        this.onStatusChange = onStatusChange
        this.documentLoader = params.documentLoader
        this.request = params.request
    }
}

