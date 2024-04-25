package com.rizzi.bouquet.compose.state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.net.toUri
import com.rizzi.bouquet.DocumentResource
import com.rizzi.bouquet.loader.DocumentRequest

@OptIn(ExperimentalFoundationApi::class)
class HorizontalPdfReaderState(
    request: DocumentRequest,
    zoomEnabled: Boolean = false,
    isAccessibleEnable: Boolean = false,
) : PdfReaderState(request, zoomEnabled, isAccessibleEnable) {

    internal var pagerState: PagerState = PagerStateImpl(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        pageCount
    }

    override val currentPage: Int
        get() = pagerState.currentPage

    override val isScrolling: Boolean
        get() = pagerState.isScrollInProgress

    companion object {
        val Saver: Saver<HorizontalPdfReaderState, *> = listSaver(
            save = {
                val resource = it.file?.let { file ->
                    DocumentResource.Local(
                        file.toUri()
                    )
                } ?: it.request.type

                listOf(
                    resource,
                    it.zoomEnabled,
                    it.isAccessibleEnable,
                    it.pagerState.currentPage,
                )
            },
            restore = {
                HorizontalPdfReaderState(
                    DocumentRequest(it[0] as DocumentResource),
                    it[1] as Boolean,
                    it[2] as Boolean
                ).apply {
                    pagerState = PagerStateImpl(
                        initialPage = it[3] as Int,
                        initialPageOffsetFraction = 0f,
                        updatedPageCount = {
                            pageCount
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun rememberHorizontalPdfReaderState(
    request: DocumentRequest,
    zoomEnabled: Boolean = true,
    isAccessibleEnable: Boolean = false,
): HorizontalPdfReaderState {
    return rememberSaveable(saver = HorizontalPdfReaderState.Saver) {
        HorizontalPdfReaderState(request, zoomEnabled, isAccessibleEnable)
    }
}
