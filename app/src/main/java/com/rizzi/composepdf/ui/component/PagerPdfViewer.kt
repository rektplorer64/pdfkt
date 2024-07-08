package com.rizzi.composepdf.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.rizzi.bouquet.PageContent
import com.rizzi.bouquet.compose.BlankPage
import com.rizzi.bouquet.compose.LazyPdfPageColumn
import com.rizzi.bouquet.compose.LazyPdfPageRow
import com.rizzi.bouquet.compose.LazyReaderListScope
import com.rizzi.bouquet.compose.pages
import com.rizzi.bouquet.compose.state.ResultStatus
import com.rizzi.bouquet.compose.state.LazyListPdfReaderState
import com.rizzi.bouquet.compose.state.PagerPdfReaderState

@Composable
fun PagerPdfViewer(
    state: PagerPdfReaderState,
    onError: (Throwable) -> Unit,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Vertical,
) {
    LaunchedEffect(key1 = state.status) {
        val status = state.status
        if (status is ResultStatus.Error) {
            onError(status.throwable)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopStart
    ) {
        val offsetX by remember {
            derivedStateOf { state.zoomState.offsetX }
        }

        val offsetY by remember {
            derivedStateOf { state.zoomState.offsetY }
        }

        when (orientation) {
            Orientation.Vertical -> {
                // TODO: Implement VerticalPdfPager
                // LazyPdfPageColumn(
                //     horizontalAlignment = Alignment.CenterHorizontally,
                //     contentPadding = PaddingValues(16.dp),
                //     verticalArrangement = Arrangement.spacedBy(16.dp),
                //     state = state,
                //     modifier = Modifier
                //         .fillMaxSize()
                //         .background(color = Color.Gray)
                // ) {
                //     content(state = state)
                // }
            }
            Orientation.Horizontal -> {
                // TODO: Implement HorizontalPdfPager
                // LazyPdfPageRow(
                //     contentPadding = PaddingValues(16.dp),
                //     verticalAlignment = Alignment.CenterVertically,
                //     horizontalArrangement = Arrangement.spacedBy(16.dp),
                //     state = state,
                //     modifier = Modifier
                //         .fillMaxSize()
                //         .background(color = Color.Gray)
                // ) {
                //     content(state = state)
                // }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val progress by rememberUpdatedState(
                (state.status as? ResultStatus.Loading)?.progress ?: 0f
            )
            LinearProgressIndicator(
                progress = progress / 100f,
                color = Color.Red,
                backgroundColor = Color.Green,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            )

            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .wrapContentSize()
                    .animateContentSize(),
                shape = RoundedCornerShape(16.dp)
            ) {
                val currentPage by remember {
                    derivedStateOf { state.currentPage }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Page: $currentPage/${state.pageCount} " + run {
                            (if (state.isScrolling) "Scrolling" else "Stationary").let { "($it)" }
                        }
                    )
                }
            }

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomStart),
                visible = state.zoomState.scale > 1f
            ) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentSize()
                        .animateContentSize(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Zooming (x${state.zoomState.scale})",
                            style = MaterialTheme.typography.h6
                        )

                        ProvideTextStyle(MaterialTheme.typography.caption) {
                            Text(text = "Offset X: $offsetX")
                            Text(text = "Offset Y: $offsetY")
                        }
                    }
                }
            }
        }

        if (state.status is ResultStatus.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

private fun LazyReaderListScope.content(state: LazyListPdfReaderState) {
    item {
        Button(onClick = { state.refresh() }) {
            Text("Refresh")
        }
    }

    state.renderer?.let {
        pages(it) { _, content ->
            when (content) {
                is PageContent.Content -> {
                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(content.bitmap)
                            .crossfade(true)
                            .build(),
                    )
                    Image(
                        painter = painter,
                        contentDescription = content.contentDescription,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .aspectRatio(content.bitmap.width / content.bitmap.height.toFloat())
                            .fillParentMaxWidth()
                    )
                }

                is PageContent.Blank -> BlankPage(
                    size = DpSize(
                        width = with(LocalDensity.current) {
                            content.width.toDp()
                        },
                        height = with(LocalDensity.current) {
                            content.height.toDp()
                        }
                    ),
                )
            }
        }
    }
}

