package com.rizzi.bouquet.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.rizzi.bouquet.PageContent
import com.rizzi.bouquet.PageRecyclingEffects
import com.rizzi.bouquet.PdfDocumentRenderer
import com.rizzi.bouquet.compose.state.LazyListPdfReaderState
import com.rizzi.bouquet.compose.state.absoluteViewportCenterY
import com.rizzi.bouquet.loader.DocumentLoader
import com.rizzi.bouquet.compose.state.relativeViewportCenterY


const val SHOW_CURRENT_PAGE_THRESHOLD_LINES = false
const val DOCUMENT_PAGE_CONTENT_TYPE = "com.rizzi.bouquet.compose.document"

typealias LazyReaderListScope = LazyListScope

fun LazyReaderListScope.pages(
    renderer: PdfDocumentRenderer,
    itemContent: @Composable LazyItemScope.(index: Int, item: PageContent) -> Unit
) {
//    requireNotNull(renderer) {
//        "VerticalPdfReaderState.renderer must be initialized (non-null) before rendering pages!"
//    }

    items(
        count = renderer.pageCount,
        key = { i -> renderer.pageLists[i].index },
        contentType = { DOCUMENT_PAGE_CONTENT_TYPE }
    ) { i ->
        val pageContent by renderer.pageLists[i].stateFlow.collectAsState()

        PageRecyclingEffects(
            renderer = renderer,
            pageIndex = i
        )

        itemContent(i, pageContent)
    }
}

fun LazyReaderListScope.pagesIndexed(
    renderer: PdfDocumentRenderer,
    itemContent: @Composable LazyItemScope.(index: Int, item: PageContent) -> Unit
) {
    items(
        count = renderer.pageCount,
        key = { i -> renderer.pageLists[i].index },
        contentType = { DOCUMENT_PAGE_CONTENT_TYPE }
    ) { i ->
        val pageContent by renderer.pageLists[i].stateFlow.collectAsState()

        PageRecyclingEffects(
            renderer = renderer,
            pageIndex = i
        )

        itemContent(i, pageContent)
    }
}

@Composable
fun LazyPdfPageColumn(
    state: LazyListPdfReaderState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    documentLoader: DocumentLoader? = null,
    content: LazyReaderListScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        DocumentReaderDisposableEffect(
            state = state,
            documentLoader = documentLoader ?: state.documentLoader,
            viewportSize = IntSize(
                constraints.maxWidth,
                constraints.maxHeight
            )
        )

        state.renderer?.let { _ ->
            Box(modifier = Modifier.readerGesture(state, constraints)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = horizontalAlignment,
                    state = state.lazyState,
                    contentPadding = contentPadding,
                    reverseLayout = reverseLayout,
                    verticalArrangement = verticalArrangement,
                    flingBehavior = flingBehavior,
                    userScrollEnabled = userScrollEnabled,
                    content = content
                )

                if (SHOW_CURRENT_PAGE_THRESHOLD_LINES) {
                    val absoluteCenterY by remember {
                        derivedStateOf { state.absoluteViewportCenterY() }
                    }
                    val relativeCenterY by remember {
                        derivedStateOf { state.relativeViewportCenterY() }
                    }

                    Divider(
                        thickness = 4.dp,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset {
                                IntOffset(0, relativeCenterY.toInt())
                            }
                    )
                    Divider(
                        thickness = 2.dp,
                        color = Color.Blue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset {
                                IntOffset(0, absoluteCenterY.toInt())
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun LazyPdfPageRow(
    state: LazyListPdfReaderState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal =
        if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    documentLoader: DocumentLoader? = null,
    content: LazyReaderListScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        DocumentReaderDisposableEffect(
            state = state,
            documentLoader = documentLoader ?: state.documentLoader,
            viewportSize = IntSize(
                constraints.maxWidth,
                constraints.maxHeight
            )
        )

        state.renderer?.let { _ ->
            Box(modifier = Modifier.readerGesture(state, constraints)) {
                LazyRow (
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = horizontalArrangement,
                    verticalAlignment = verticalAlignment,
                    state = state.lazyState,
                    contentPadding = contentPadding,
                    reverseLayout = reverseLayout,
                    flingBehavior = flingBehavior,
                    userScrollEnabled = userScrollEnabled,
                    content = content
                )

                if (SHOW_CURRENT_PAGE_THRESHOLD_LINES) {
                    val absoluteCenterY by remember {
                        derivedStateOf { state.absoluteViewportCenterY() }
                    }
                    val relativeCenterY by remember {
                        derivedStateOf { state.relativeViewportCenterY() }
                    }

                    Divider(
                        thickness = 4.dp,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset {
                                IntOffset(0, relativeCenterY.toInt())
                            }
                    )
                    Divider(
                        thickness = 2.dp,
                        color = Color.Blue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset {
                                IntOffset(0, absoluteCenterY.toInt())
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun BlankPage(
    size: DpSize,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = Color.White)
    )
}


