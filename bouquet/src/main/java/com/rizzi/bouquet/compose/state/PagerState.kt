package com.rizzi.bouquet.compose.state

import androidx.compose.foundation.pager.PagerLayoutInfo
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.center
import androidx.compose.ui.util.fastLastOrNull
import coil.compose.DefaultModelEqualityDelegate
import coil.compose.EqualityDelegate
import com.rizzi.bouquet.loader.DocumentLoader
import com.rizzi.bouquet.loader.DocumentRequest


internal val PagerLayoutInfo.viewportCenterYPx: Float
    get() = viewportSize.center.y.toFloat()

internal fun PagerPdfReaderState.absoluteViewportCenterY(): Float {
    return pagerState?.layoutInfo?.viewportCenterYPx ?: return 0f
}

internal fun PagerPdfReaderState.relativeViewportCenterY(): Float {
    val middleOfTheScreen = pagerState?.layoutInfo?.viewportCenterYPx ?: return 0f
    val finalMiddleOfTheScreen = middleOfTheScreen - (zoomState.offsetY / zoomState.scale)

    return finalMiddleOfTheScreen
}

class PagerPdfReaderState(
    request: DocumentRequest,
    documentLoader: DocumentLoader,
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

    var pagerState: PagerState? = null
        internal set

    override val currentPage: Int
        get() = currentPage()

    override val isScrolling: Boolean
        get() = pagerState?.isScrollInProgress == true

    private fun currentPage(): Int {
        return renderer?.let { _ ->
            with(pagerState ?: return@let null) {
                val finalMiddleOfTheScreen = relativeViewportCenterY()

                val currentPageVisibleItem = layoutInfo.visiblePagesInfo
//                    .fastFilter { it.contentType == DOCUMENT_PAGE_CONTENT_TYPE }
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
fun rememberViewPagerPdfReaderState(
    request: DocumentRequest,
    documentLoader: DocumentLoader = run {
        val context = LocalContext.current
        remember { DocumentLoader.Builder(context).build() }
    },
    zoomEnabled: Boolean = true,
    accessibleEnabled: Boolean = false,
    onStatusChange: StatusChangeCallback? = null,
    requestEqualityDelegate: EqualityDelegate = DefaultModelEqualityDelegate,
): PagerPdfReaderState {
    val params = PdfReaderStateParams(
        request = request,
        requestEqualityDelegate = requestEqualityDelegate,
        documentLoader = documentLoader
    )

    val state = remember {
        PagerPdfReaderState(
            request = request,
            documentLoader = params.documentLoader,
            zoomEnabled = zoomEnabled,
            accessibilityEnabled = accessibleEnabled,
            onStatusChange = onStatusChange,
        )
    }.apply {
        this.onStatusChange = onStatusChange
        this.documentLoader = params.documentLoader
        this.request = params.request
    }

    // TODO: Accept other params of rememberPagerState
    val pagerState = rememberPagerState {
        state.pageCount
    }

    state.pagerState = pagerState

    return state
}

