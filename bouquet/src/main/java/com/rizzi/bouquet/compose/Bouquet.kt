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
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.rizzi.bouquet.PageContent
import com.rizzi.bouquet.PageRecyclingEffects
import com.rizzi.bouquet.PdfDocumentRenderer
import com.rizzi.bouquet.compose.state.ResultStatus
import com.rizzi.bouquet.compose.state.VerticalPdfReaderState
import com.rizzi.bouquet.compose.state.absoluteViewportCenterY
import com.rizzi.bouquet.loader.DocumentLoader
import com.rizzi.bouquet.loader.DocumentRequest
import com.rizzi.bouquet.loader.EventListener
import com.rizzi.bouquet.compose.state.relativeViewportCenterY


const val SHOW_CURRENT_PAGE_THRESHOLD_LINES = false

fun LazyReaderListScope.pages(
    itemContent: @Composable LazyItemScope.(index: Int, item: PageContent) -> Unit
) = items(
    count = renderer.pageCount,
    key = { i -> renderer.pageLists[i].index }
) { i ->
    val pageContent by this@pages.renderer.pageLists[i].stateFlow.collectAsState()

    PageRecyclingEffects(
        renderer = this@pages.renderer,
        pageIndex = i
    )

    itemContent(i, pageContent)
}

fun LazyReaderListScope.pagesIndexed(
    itemContent: @Composable LazyItemScope.(index: Int, item: PageContent) -> Unit
) = items(
    count = renderer.pageCount,
    key = { i -> renderer.pageLists[i].index }
) { i ->
    val pageContent by this@pagesIndexed.renderer.pageLists[i].stateFlow.collectAsState()

    PageRecyclingEffects(
        renderer = this@pagesIndexed.renderer,
        pageIndex = i
    )

    itemContent(i, pageContent)
}

class LazyReaderListScope internal constructor(scope: LazyListScope, internal val renderer: PdfDocumentRenderer) : LazyListScope by scope

@Composable
fun LazyPdfPageColumn(
    state: VerticalPdfReaderState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyReaderListScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        val context = LocalContext.current
        DocumentReaderDisposableEffect(
            state = state,
            documentLoader = remember {
                DocumentLoader
                    .Builder(context)
                    .eventListener(
                        object : EventListener {
                            override fun onStart(request: DocumentRequest) {
                                state.status = ResultStatus.Loading(progress = 0f)
                            }

                            override fun onLoading(request: DocumentRequest, progress: Float) {
                                state.status = ResultStatus.Loading(progress = progress)
                            }

                            override fun onSuccess(request: DocumentRequest) {

                            }

                            override fun onCancel(request: DocumentRequest) {
                            }

                            override fun onFailure(request: DocumentRequest, throwable: Throwable) {

                            }
                        }
                    )
                    .build()
            },
            viewportSize = IntSize(
                constraints.maxWidth,
                constraints.maxHeight
            )
        )

        state.renderer?.let { renderer ->
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
                ) {
                    val scope = LazyReaderListScope(
                        scope = this,
                        renderer = renderer
                    )
                    scope.content()

//                    items(
//                        count = renderer.pageCount,
//                        key = { renderer.pageLists[it].index }
//                    ) {
//                        val pageContent by renderer.pageLists[it].stateFlow.collectAsState()
//
//                        PageRecyclingEffects(
//                            renderer = renderer,
//                            pageIndex = it
//                        )
//
//                        when (val content = pageContent) {
//                            is PageContentInt.PageContent -> {
//                                val painter = rememberAsyncImagePainter(
//                                    model = ImageRequest.Builder(LocalContext.current)
//                                        .data(content.bitmap)
//                                        .crossfade(true)
//                                        .build(),
//                                )
//
//                                Image(
//                                    painter = painter,
//                                    contentDescription = content.contentDescription,
//                                    contentScale = ContentScale.FillWidth,
//                                    modifier = Modifier
//                                        .clip(RoundedCornerShape(4.dp))
//                                        .aspectRatio(content.bitmap.width / content.bitmap.height.toFloat())
//                                        .fillParentMaxWidth()
//                                )
//                            }
//
//                            is PageContentInt.BlankPage -> BlackPage(
//                                width = content.width,
//                                height = content.height
//                            )
//                        }
//                    }
                }

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

//@Composable
//fun VerticalPDFReader(
//    state: VerticalPdfReaderState,
//    modifier: Modifier
//) {
//    BoxWithConstraints(
//        modifier = modifier,
//        contentAlignment = Alignment.TopCenter
//    ) {
//        val context = LocalContext.current
//        DocumentReaderDisposableEffect(
//            state = state,
//            documentLoader = remember {
//                DocumentLoader.Builder(context).build()
//            },
//            viewportSize = IntSize(
//                constraints.maxWidth,
//                constraints.maxHeight
//            )
//        )
//
//        state.renderer?.let { pdf ->
//            Box(modifier = Modifier.readerGesture(state, constraints)) {
//                LazyColumn(
//                    modifier = Modifier.fillMaxSize(),
//                    state = state.lazyState,
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    contentPadding = PaddingValues(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(16.dp),
//                ) {
//                    items(
//                        count = pdf.pageCount,
//                        key = { pdf.pageLists[it].index }
//                    ) {
//                        val pageContent = pdf.pageLists[it].stateFlow.collectAsState().value
//                        DisposableEffect(key1 = Unit) {
//                            pdf.pageLists[it].load()
//                            onDispose {
//                                pdf.pageLists[it].recycle()
//                            }
//                        }
//
//                        when (pageContent) {
//                            is PageContentInt.PageContent -> {
//                                val painter = rememberAsyncImagePainter(
//                                    model = ImageRequest.Builder(LocalContext.current)
//                                        .data(pageContent.bitmap)
//                                        .crossfade(true)
//                                        .build(),
//                                )
//
//                                Image(
//                                    painter = painter,
//                                    contentDescription = pageContent.contentDescription,
//                                    contentScale = ContentScale.FillWidth,
//                                    modifier = Modifier
//                                        .clip(RoundedCornerShape(4.dp))
//                                        .aspectRatio(pageContent.bitmap.width / pageContent.bitmap.height.toFloat())
//                                        .fillParentMaxWidth()
//                                )
//                            }
//
//                            is PageContentInt.BlankPage -> BlackPage(
//                                width = pageContent.width,
//                                height = pageContent.height
//                            )
//                        }
//                    }
//                }
//
//                if (SHOW_CURRENT_PAGE_THRESHOLD_LINES) {
//                    val absoluteCenterY by remember {
//                        derivedStateOf { state.absoluteViewportCenterY() }
//                    }
//                    val relativeCenterY by remember {
//                        derivedStateOf { state.relativeViewportCenterY() }
//                    }
//
//                    Divider(
//                        thickness = 4.dp,
//                        color = Color.Red,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .offset {
//                                IntOffset(0, relativeCenterY.toInt())
//                            }
//                    )
//                    Divider(
//                        thickness = 2.dp,
//                        color = Color.Blue,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .offset {
//                                IntOffset(0, absoluteCenterY.toInt())
//                            }
//                    )
//                }
//            }
//        }
//    }
//}

@Composable
fun Int.dp(): Dp {
    val density = LocalDensity.current.density
    return (this / density).dp
}


//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun HorizontalPDFReader(
//    state: HorizontalPdfReaderState,
//    modifier: Modifier
//) {
//    BoxWithConstraints(
//        modifier = modifier,
//        contentAlignment = Alignment.TopCenter
//    ) {
//        val context = LocalContext.current
//        DocumentReaderDisposableEffect(
//            state = state,
//            documentLoader = remember {
//                DocumentLoader.Builder(context).build()
//            },
//            viewportSize = IntSize(
//                constraints.maxWidth,
//                constraints.maxHeight
//            )
//        )
//
//        state.renderer?.let { pdf ->
//            HorizontalPager(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .tapToZoomHorizontal(state, constraints),
//                state = state.pagerState,
//                userScrollEnabled = state.zoomState.scale == 1f
//            ) { page ->
//                val pageContent = pdf.pageLists[page].stateFlow.collectAsState().value
//                DisposableEffect(key1 = Unit) {
//                    pdf.pageLists[page].load()
//                    onDispose {
//                        pdf.pageLists[page].recycle()
//                    }
//                }
//                when (pageContent) {
//                    is PageContentInt.PageContent -> {
//                        PdfImage(
//                            bitmap = pageContent.bitmap,
//                            contentDescription = pageContent.contentDescription
//                        )
//                    }
//
//                    is PageContentInt.BlankPage -> BlackPage(
//                        width = pageContent.width,
//                        height = pageContent.height
//                    )
//                }
//            }
//        }
//    }
//}

//private fun load(
//    coroutineScope: CoroutineScope,
//    context: Context,
//    download: suspend (DocumentResource.Remote) -> ResponseBody,
//    state: PdfReaderState,
//    viewportSize: IntSize,
//    orientation: Orientation,
//) {
//    runCatching {
//        val file = state.file
//        if (file != null) {
//            coroutineScope.launch(Dispatchers.IO) {
//                runCatching {
//                    val pFD =
//                        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
//                    val textForEachPage =
//                        if (state.isAccessibleEnable) getTextByPage(context, pFD) else emptyList()
//                    state.renderer = PdfDocumentRenderer(
//                        fileDescriptor = pFD,
//                        textForEachPage = textForEachPage,
//                        viewportSize = viewportSize,
//                        orientation = orientation
//                    )
//                }.onFailure {
//                    state.status = ResultStatus.Error(Unit, it)
//                }
//            }
//        } else {
//            when (val res = state.resource) {
//                is DocumentResource.Local -> {
//                    coroutineScope.launch(Dispatchers.IO) {
//                        runCatching {
//                            context.contentResolver.openFileDescriptor(res.uri, "r")?.let {
//                                val textForEachPage = if (state.isAccessibleEnable) {
//                                    getTextByPage(context, it)
//                                } else emptyList()
//                                state.renderer = PdfDocumentRenderer(
//                                    fileDescriptor = it,
//                                    textForEachPage = textForEachPage,
//                                    viewportSize = viewportSize,
//                                    orientation = orientation
//                                )
//                                state.file = context.uriToFile(res.uri)
//                            } ?: throw IOException("File not found")
//                        }.onFailure {
//                            state.status = ResultStatus.Error(null, throwable = it)
//                        }
//                    }
//                }
//
//                is DocumentResource.Remote -> {
//                    coroutineScope.launch(Dispatchers.IO) {
//                        runCatching {
//                            val bufferSize = 8192
//                            var downloaded = 0
//
//                            val file = File(
//                                context.cacheDir,
//                                generateFileName(res.url.hashCode().toHexString())
//                            )
//
//                            val hasCache = file.isFile && file.length() > 0
//                            if (!hasCache) {
//                                val response = download(res)
//                                val byteStream = response.byteStream()
//
//                                byteStream.use { input ->
//                                    file.outputStream().use { output ->
//                                        val totalBytes = response.contentLength()
//                                        var data = ByteArray(bufferSize)
//                                        var count = input.read(data)
//                                        while (count != -1) {
//                                            if (totalBytes > 0) {
//                                                downloaded += bufferSize
//                                                state.mLoadPercent = (downloaded * (100 / totalBytes.toFloat())).toInt()
//                                            }
//                                            output.write(data, 0, count)
//                                            data = ByteArray(bufferSize)
//                                            count = input.read(data)
//                                        }
//                                    }
//                                }
//                            }
//
//                            val fileDescriptor = ParcelFileDescriptor.open(
//                                file,
//                                ParcelFileDescriptor.MODE_READ_ONLY
//                            )
//                            val textForEachPage = if (state.isAccessibleEnable) {
//                                getTextByPage(context, fileDescriptor)
//                            } else emptyList()
//
//                            state.renderer = PdfDocumentRenderer(
//                                fileDescriptor = fileDescriptor,
//                                textForEachPage = textForEachPage,
//                                viewportSize = viewportSize,
//                                orientation = orientation
//                            )
//                            state._file = file
//                        }.onFailure {
//                            state._throwable = it
//                        }
//                    }
//                }
//
//                is DocumentResource.Base64 -> {
//                    coroutineScope.launch(Dispatchers.IO) {
//                        runCatching {
//                            val file = context.base64ToPdf(res.file)
//                            val pFD = ParcelFileDescriptor.open(
//                                file,
//                                ParcelFileDescriptor.MODE_READ_ONLY
//                            )
//                            val textForEachPage = if (state.isAccessibleEnable) {
//                                getTextByPage(context, pFD)
//                            } else emptyList()
//                            state.renderer = PdfDocumentRenderer(
//                                fileDescriptor = pFD,
//                                textForEachPage = textForEachPage,
//                                viewportSize = viewportSize,
//                                orientation = orientation
//                            )
//                            state._file = file
//                        }.onFailure {
//                            state._throwable = it
//                        }
//                    }
//                }
//
//                is DocumentResource.Asset -> {
//                    coroutineScope.launch(Dispatchers.IO) {
//                        runCatching {
//                            val bufferSize = 8192
//                            val inputStream = context.resources.openRawResource(res.assetId)
//                            val outFile = File(context.cacheDir, generateFileName())
//                            inputStream.use { input ->
//                                outFile.outputStream().use { output ->
//                                    var data = ByteArray(bufferSize)
//                                    var count = input.read(data)
//                                    while (count != -1) {
//                                        output.write(data, 0, count)
//                                        data = ByteArray(bufferSize)
//                                        count = input.read(data)
//                                    }
//                                }
//                            }
//                            val pFD = ParcelFileDescriptor.open(
//                                outFile,
//                                ParcelFileDescriptor.MODE_READ_ONLY
//                            )
//                            val textForEachPage = if (state.isAccessibleEnable) {
//                                getTextByPage(context, pFD)
//                            } else emptyList()
//                            state.renderer = PdfDocumentRenderer(
//                                fileDescriptor = pFD,
//                                textForEachPage = textForEachPage,
//                                viewportSize = viewportSize,
//                                orientation = orientation
//                            )
//                            state._file = outFile
//                        }.onFailure {
//                            state._throwable = it
//                        }
//                    }
//                }
//            }
//        }
//    }.onFailure {
//        state._throwable = it
//    }
//}

@Composable
fun BlackPage(
    width: Int,
    height: Int
) {
    Box(
        modifier = Modifier
            .size(
                width = width.dp(),
                height = height.dp()
            )
            .background(color = Color.White)
    )
}


