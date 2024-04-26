package com.rizzi.composepdf

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.rizzi.bouquet.DocumentResource
import com.rizzi.bouquet.PageContent
import com.rizzi.bouquet.compose.BlackPage
import com.rizzi.bouquet.compose.LazyPdfPageColumn
import com.rizzi.bouquet.compose.pages
import com.rizzi.bouquet.compose.state.HorizontalPdfReaderState
import com.rizzi.bouquet.compose.state.ResultStatus
import com.rizzi.bouquet.compose.state.VerticalPdfReaderState
import com.rizzi.composepdf.ui.theme.ComposePDFTheme
import java.io.File


class MainActivity : ComponentActivity() {

    private val viewModel: BouquetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.clearResource()
            }
        })
        setContent {
            ComposePDFTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val scaffoldState = rememberScaffoldState()
                    val state = viewModel.stateFlow.collectAsState()
                    Scaffold(
                        topBar = {
                            TopAppBar()
                        },
                        scaffoldState = scaffoldState,
                        floatingActionButton = {
                            state.value?.file?.let {
                                FloatingActionButton(
                                    onClick = {
                                        shareFile(it)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_share),
                                        contentDescription = "share"
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            when (val actualState = state.value) {
                                null -> SelectionView()
                                is VerticalPdfReaderState -> PDFView(
                                    pdfState = actualState,
                                    scaffoldState = scaffoldState
                                )
                                is HorizontalPdfReaderState -> HPDFView(
                                    pdfState = actualState,
                                    scaffoldState = scaffoldState
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TopAppBar() {
        TopAppBar(
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.h6
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun SelectionElement(
        title: String,
        text: String,
        onClick: () -> Unit
    ) {
        Card(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 4.dp
                    ),
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = text,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 16.dp
                    ),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }

    @Composable
    fun SelectionView() {
        Column(modifier = Modifier.fillMaxSize()) {
            SelectionElement(
                title = "Open Base64",
                text = "Try to open a base64 pdf"
            ) {
                viewModel.openResource(
                    DocumentResource.Base64(
                        this@MainActivity.getString(R.string.base64_pdf)
                    )
                )
            }
            SelectionElement(
                title = "Open Remote file",
                text = "Open a remote file from url"
            ) {
                viewModel.openResource(
                    DocumentResource.Remote(
                        url = this@MainActivity.getString(
                            R.string.pdf_url
                        ),
                        headers = hashMapOf("headerKey" to "headerValue")
                    )
                )
            }
            SelectionElement(
                title = "Open Local file",
                text = "Open a file in device memory"
            ) {
                openDocumentPicker()
            }
            SelectionElement(
                title = "Open asset file",
                text = "Open asset file in raw folder"
            ) {
                viewModel.openResource(
                    DocumentResource.Asset(R.raw.lorem_ipsum)
                )
            }
            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(text = "List view")
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = viewModel.switchState.value,
                    onCheckedChange = {
                        viewModel.switchState.value = it
                    },
                    colors = SwitchDefaults.colors(
                        uncheckedThumbColor = MaterialTheme.colors.secondaryVariant,
                        uncheckedTrackColor = MaterialTheme.colors.secondaryVariant,
                        uncheckedTrackAlpha = 0.54f
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Pager view")
            }
        }
    }

    @Composable
    fun PDFView(
        pdfState: VerticalPdfReaderState,
        scaffoldState: ScaffoldState
    ) {
        Box(
            contentAlignment = Alignment.TopStart
        ) {
            val offsetY by remember {
                derivedStateOf { pdfState.zoomState.offsetY }
            }

            LazyPdfPageColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = pdfState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Gray)
            ) {
                item {
                    Button(onClick = { pdfState.refresh() }) {
                        Text("Refresh")
                    }
                }

                pdfState.renderer?.let {
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
                            is PageContent.Blank -> BlackPage(
                                width = content.width,
                                height = content.height
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                val progress by rememberUpdatedState((pdfState.status as? ResultStatus.Loading)?.progress ?: 0f)
                LinearProgressIndicator(
                    progress = progress / 100f,
                    color = Color.Red,
                    backgroundColor = Color.Green,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colors.surface.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.medium
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        SideEffect {
                            val offsets = pdfState.lazyState.layoutInfo.visibleItemsInfo.mapIndexed { index, it ->
                                index to it.offset
                            }.joinToString(", ") { "${it.first}: ${it.second}" }
                            println("[X] offsets: $offsets")
                        }

                        val p by remember {
                            derivedStateOf { pdfState.currentPage }
                        }
                        Text(
                            text = "Page: $p/${pdfState.pageCount}",
                            modifier = Modifier.padding(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 4.dp,
                                top = 8.dp
                            )
                        )

                        Text(text = "Offset: ${pdfState.zoomState.offsetX} ${pdfState.zoomState.offsetY}", fontSize = 22.sp)
//                        Text(
//                            text = "Offset X: ${pdfState._offsetX.asState().value}",
//                            modifier = Modifier.padding(
//                                start = 8.dp,
//                                end = 8.dp,
//                                bottom = 4.dp,
//                                top = 8.dp
//                            )
//                        )

                        Text(
                            text = "Offset Y: $offsetY",
                            modifier = Modifier.padding(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 4.dp,
                                top = 8.dp
                            )
                        )
                        Text(
                            text = if (pdfState.isScrolling) {
                                "Scrolling"
                            } else {
                                "Stationary"
                            },
                            color = if (pdfState.isScrolling) {
                                MaterialTheme.colors.onSurface
                            } else {
                                MaterialTheme.colors.error
                            },
                            modifier = Modifier.padding(
                                start = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp,
                                top = 4.dp
                            )
                        )
                    }
                }
            }
            LaunchedEffect(key1 = pdfState.status) {

                val status = pdfState.status
                if (status is ResultStatus.Error) {
                    status.throwable.let {
                        scaffoldState.snackbarHostState.showSnackbar(
                            message = it.message ?: "Error"
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun HPDFView(
        pdfState: HorizontalPdfReaderState,
        scaffoldState: ScaffoldState
    ) {
        Box(
            contentAlignment = Alignment.TopStart
        ) {
//            HorizontalPDFReader(
//                state = pdfState,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(color = Color.Gray)
//            )
//            Column(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                LinearProgressIndicator(
//                    progress = pdfState.loadPercent / 100f,
//                    color = Color.Red,
//                    backgroundColor = Color.Green,
//                    modifier = Modifier.fillMaxWidth()
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Row {
//                    Spacer(modifier = Modifier.width(16.dp))
//                    Column(
//                        modifier = Modifier
//                            .background(
//                                color = MaterialTheme.colors.surface.copy(alpha = 0.5f),
//                                shape = MaterialTheme.shapes.medium
//                            ),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Text(
//                            text = "Page: ${pdfState.currentPage}/${pdfState.pageCount}",
//                            modifier = Modifier.padding(
//                                start = 8.dp,
//                                end = 8.dp,
//                                bottom = 4.dp,
//                                top = 8.dp
//                            )
//                        )
//                        Text(
//                            text = if (pdfState.isScrolling) {
//                                "Scrolling"
//                            } else {
//                                "Stationary"
//                            },
//                            color = if (pdfState.isScrolling) {
//                                MaterialTheme.colors.onSurface
//                            } else {
//                                MaterialTheme.colors.error
//                            },
//                            modifier = Modifier.padding(
//                                start = 8.dp,
//                                end = 8.dp,
//                                bottom = 8.dp,
//                                top = 4.dp
//                            )
//                        )
//                        Text(text = "${pdfState.scale}")
//                    }
//                }
//            }
//            LaunchedEffect(key1 = pdfState.throwable) {
//                pdfState.throwable?.let {
//                    scaffoldState.snackbarHostState.showSnackbar(
//                        message = it.message ?: "Error"
//                    )
//                }
//            }
        }
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
    }

    private fun openDocument(documentUri: Uri) {
        documentUri.path?.let {
            viewModel.openResource(
                DocumentResource.Local(
                    documentUri
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { documentUri ->
                contentResolver.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                openDocument(documentUri)
            }
        }
    }

    private fun shareFile(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${this.packageName}.fileprovider",
            file
        )
        val intent = ShareCompat.IntentBuilder.from(this)
            .setType("application/pdf")
            .setStream(uri)
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }
}

private const val OPEN_DOCUMENT_REQUEST_CODE = 0x33
