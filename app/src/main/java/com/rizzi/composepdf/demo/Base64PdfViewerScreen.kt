package com.rizzi.composepdf.demo

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rizzi.bouquet.DocumentResource
import com.rizzi.bouquet.compose.state.LazyListPdfReaderState
import com.rizzi.bouquet.compose.state.rememberLazyListPdfReaderState
import com.rizzi.bouquet.loader.DocumentRequest
import com.rizzi.composepdf.R
import com.rizzi.composepdf.ui.component.OrientationToggle
import com.rizzi.composepdf.ui.component.LazyPdfListViewer
import kotlinx.coroutines.launch

@Composable
fun Base64PdfViewerScreen(
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListPdfReaderState = rememberLazyListPdfReaderState(
        request = DocumentRequest(DocumentResource.Base64(stringResource(R.string.base64_pdf)))
    ),
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var orientation by rememberSaveable {
        mutableStateOf(Orientation.Vertical)
    }
    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Base64") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    OrientationToggle(
                        value = orientation,
                        onValueChange = {
                            orientation = it
                        }
                    )
                }
            )
        },
    ) { pv ->
        LazyPdfListViewer(
            modifier = Modifier.padding(pv),
            state = state,
            orientation = orientation,
            onError = {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = it.message ?: "Error"
                    )
                }
            }
        )
    }
}

