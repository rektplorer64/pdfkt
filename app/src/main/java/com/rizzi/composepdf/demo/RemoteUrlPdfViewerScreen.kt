package com.rizzi.composepdf.demo

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.rizzi.bouquet.DocumentResource
import com.rizzi.bouquet.compose.state.LazyListPdfReaderState
import com.rizzi.bouquet.compose.state.rememberLazyListPdfReaderState
import com.rizzi.bouquet.loader.DocumentRequest
import com.rizzi.composepdf.R
import com.rizzi.composepdf.ui.component.OrientationToggle
import com.rizzi.composepdf.ui.component.LazyPdfListViewer
import kotlinx.coroutines.launch

@Composable
fun RemoteUrlPdfViewerScreen(
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val initialUrl = stringResource(R.string.pdf_url)

    var urlText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initialUrl, selection = TextRange(initialUrl.length)))
    }

    var appliedUrl by rememberSaveable {
        mutableStateOf(initialUrl)
    }

    val state: LazyListPdfReaderState = rememberLazyListPdfReaderState(
        request = DocumentRequest(DocumentResource.Remote(appliedUrl))
    )

    var orientation by rememberSaveable {
        mutableStateOf(Orientation.Vertical)
    }

    LaunchedEffect(state.status) {
        println("[X] Status: ${state.status}")
    }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            Surface {
                Column {
                    TopAppBar(
                        title = { Text("Remote URL") },
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

                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = urlText,
                            onValueChange = { urlText = it },
                            label = { Text("Remote URL") },
                            maxLines = 1,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { appliedUrl = urlText.text }) {
                                    Icon(
                                        Icons.AutoMirrored.Default.ArrowForward,
                                        contentDescription = "Apply"
                                    )
                                }
                            }
                        )
                    }
                }
            }
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