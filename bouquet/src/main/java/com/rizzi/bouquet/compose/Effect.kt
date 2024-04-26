package com.rizzi.bouquet.compose

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.rizzi.bouquet.PdfDocumentRenderer
import com.rizzi.bouquet.compose.state.PdfReaderState
import com.rizzi.bouquet.compose.state.RenderingComponent
import com.rizzi.bouquet.compose.state.ResultStatus
import com.rizzi.bouquet.compose.state.VerticalPdfReaderState
import com.rizzi.bouquet.getTextByPage
import com.rizzi.bouquet.loader.DocumentLoader
import com.rizzi.bouquet.loader.DocumentRequest
import com.rizzi.bouquet.loader.DocumentResult
import kotlinx.coroutines.launch

@Composable
fun DocumentReaderDisposableEffect(
    state: PdfReaderState,
    documentLoader: DocumentLoader,
    viewportSize: IntSize
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    DisposableEffect(state.refresher) {
        coroutineScope.launch {
            val existingListener = state.request.listener
            val request = state.request.copy(
                listener = object : DocumentRequest.Listener {
                    override fun onStart(request: DocumentRequest) {
                        state.status = ResultStatus.Loading(progress = 0f)
                        existingListener?.onStart(request)
                    }

                    override fun onLoading(request: DocumentRequest, progress: Float) {
                        state.status = ResultStatus.Loading(progress = progress)
                        existingListener?.onLoading(request, progress)
                    }

                    override fun onSuccess(request: DocumentRequest, result: DocumentResult) {
                        existingListener?.onSuccess(request, result)
                    }

                    override fun onCancel(request: DocumentRequest) {
                        existingListener?.onCancel(request)
                    }

                    override fun onFailure(request: DocumentRequest, result: DocumentResult) {
                        existingListener?.onFailure(request, result)
                    }

                }
            )
            when (val result = documentLoader.execute(request)) {
                is DocumentResult.Error -> {
                    state.status = ResultStatus.Error(null, result.throwable)
                }

                is DocumentResult.Success -> {
                    val accessibilityTextList = if (state.isAccessibleEnable) {
                        getTextByPage(context, result.fileDescriptor)
                    } else emptyList()

                    state.status = ResultStatus.Success(
                        RenderingComponent(
                            file = result.file,
                            renderer = PdfDocumentRenderer(
                                fileDescriptor = result.fileDescriptor,
                                textForEachPage = accessibilityTextList,
                                viewportSize = viewportSize,
                                orientation = if (state is VerticalPdfReaderState) {
                                    Orientation.Vertical
                                } else {
                                    Orientation.Horizontal
                                }
                            )
                        )
                    )
                }
            }
        }
        onDispose {
            state.close()
        }
    }
}