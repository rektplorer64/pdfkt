package com.rizzi.bouquet.compose

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import com.rizzi.bouquet.compose.state.LazyListPdfReaderState
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.ScrollGesturePropagation
import net.engawapg.lib.zoomable.zoomable

internal fun Modifier.readerGesture(
    state: LazyListPdfReaderState,
    constraints: Constraints
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "verticalTapToZoom"
        properties["state"] = state
    }
) {
    val coroutineScope = rememberCoroutineScope()

    this.zoomable(
        zoomState = state.zoomState,
        zoomEnabled = state.zoomEnabled,
        scrollGesturePropagation = ScrollGesturePropagation.ContentEdge,
        onDoubleTap = { tapCenter ->
            if (!state.zoomEnabled) return@zoomable
            val center = Offset(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
            if (state.zoomState.scale > 1.0f) {
                coroutineScope.launch {
                    state.zoomState.changeScale(
                        targetScale = 1f,
                        position = center,
                    )
                }
            } else {
                coroutineScope.launch {
                    state.zoomState.changeScale(3f, position = tapCenter)
                }
            }
        }
    )
}

//fun Modifier.tapToZoomVertical(
//    state: VerticalPdfReaderState,
//    constraints: Constraints
//): Modifier = composed(
//    inspectorInfo = debugInspectorInfo {
//        name = "verticalTapToZoom"
//        properties["state"] = state
//    }
//) {
//    val coroutineScope = rememberCoroutineScope()
//
//    this
//        .pointerInput(Unit) {
//            detectTapGestures(
//                onDoubleTap = { tapCenter ->
//                    if (!state.zoomEnabled) return@detectTapGestures
//                    if (state._scale.value > 1.0f) {
//                        coroutineScope.launch {
//                            launch {
//                                state._scale.animateTo(1f)
//                            }
//                            launch {
//                                state.animateOffset(0f, 0f)
//                            }
//                        }
//                    } else {
//                        val center = Pair(constraints.maxWidth / 2, constraints.maxHeight / 2)
//                        val xDiff = (tapCenter.x - center.first) * state.scale
//                        val yDiff = ((tapCenter.y - center.second) * state.scale).coerceIn(
//                            minimumValue = -(center.second * 2f),
//                            maximumValue = (center.second * 2f)
//                        )
//
//                        coroutineScope.launch {
//                            launch {
//                                state._scale.animateTo(3f)
//                            }
//                            launch {
//                                state.animateOffset(-xDiff, -yDiff)
//                            }
//                        }
//                    }
//                }
//            )
//        }
////        .pointerInput(Unit) {
////            val decay = splineBasedDecay<Float>(this)
////            customDetectTransformGestures(
////                onGesture = { centroid, pan, zoom ->
////                    val pair = if (pan.y > 0) {
////                        if (state.lazyState.canScrollBackward) {
////                            Pair(0f, pan.y)
////                        } else {
////                            Pair(pan.y, 0f)
////                        }
////                    } else {
////                        if (state.lazyState.canScrollForward) {
////                            Pair(0f, pan.y)
////                        } else {
////                            Pair(pan.y, 0f)
////                        }
////                    }
////                    val nOffset = if (state.scale > 1f) {
////                        val maxT = (constraints.maxWidth * state.scale) - constraints.maxWidth
////                        val maxY = (constraints.maxHeight * state.scale) - constraints.maxHeight
////                        Offset(
////                            x = (state._offsetX.value + pan.x).coerceIn(
////                                minimumValue = (-maxT / 2) * 1.3f,
////                                maximumValue = (maxT / 2) * 1.3f
////                            ),
////                            y = (state._offsetY.value + pair.first).coerceIn(
////                                minimumValue = (-maxY / 2),
////                                maximumValue = (maxY / 2)
////                            )
////                        )
////                    } else {
////                        Offset(0f, 0f)
////                    }
////
////                    coroutineScope.launch {
////                        state._offsetX.snapTo(nOffset.x)
////                        state._offsetY.snapTo(nOffset.y)
////                        state.lazyState.scrollBy((-pair.second / state.scale))
////                    }
////
////                    true
////                },
////                onFling = { velocity ->
////                    coroutineScope.launch {
////                        state._offsetX.animateDecay(velocity.x, decay)
////                        state._offsetY.animateDecay(velocity.y, decay)
////                    }
////                }
////            )
////        }
//        .pointerInput(Unit) {
//            val velocityTracker = VelocityTracker()
//
//            detectTransformGestures(false) { centroid, pan, zoom, rotation ->
////                val decay = splineBasedDecay<Float>(this)
//
////                println("[X] zoom: $zoom")
//                val pair = if (pan.y > 0) {
//                    if (state.lazyState.canScrollBackward) {
//                        Pair(0f, pan.y)
//                    } else {
//                        Pair(pan.y, 0f)
//                    }
//                } else {
//                    if (state.lazyState.canScrollForward) {
//                        Pair(0f, pan.y)
//                    } else {
//                        Pair(pan.y, 0f)
//                    }
//                }
//                val nOffset = if (state.scale > 1f) {
//                    val maxT = (constraints.maxWidth * state.scale) - constraints.maxWidth
//                    val maxY = (constraints.maxHeight * state.scale) - constraints.maxHeight
//                    Offset(
//                        x = (state._offsetX.value + pan.x).coerceIn(
//                            minimumValue = (-maxT / 2) * 1.3f,
//                            maximumValue = (maxT / 2) * 1.3f
//                        ),
//                        y = (state._offsetY.value + pair.first).coerceIn(
//                            minimumValue = (-maxY / 2),
//                            maximumValue = (maxY / 2)
//                        )
//                    )
//                } else {
//                    Offset(0f, 0f)
//                }
////                velocityTracker.addPosition(
////                    change.uptimeMillis,
////                    change.position
////                )
//
//                coroutineScope.launch {
//                    state._scale.snapTo(
//                        (state._scale.value * zoom).coerceIn(
//                            state.minZoom,
//                            state.maxScale
//                        )
//                    )
////                    state._offsetX.snapTo(nOffset.x)
////                    state._offsetY.snapTo(nOffset.y)
//                    state.lazyState.scrollBy((-pair.second / state.scale))
//                }
//            }
//        }
////        .pointerInput(Unit) {
////
////        }
//        .pointerInput(Unit) {
//            val decay = splineBasedDecay<Float>(this)
//
//            val velocityTracker = VelocityTracker()
//            detectDragGestures { change, dragAmount ->
//                velocityTracker.addPosition(change.uptimeMillis, change.position)
//
//                val velocity = velocityTracker.calculateVelocity()
//
////                val targetOffsetX = decay.calculateTargetValue(
////                    state._offsetX.value,
////                    velocity.x
////                )
////
////                val targetOffsetY = decay.calculateTargetValue(
////                    state._offsetY.value,
////                    velocity.y
////                )
//
//                val maxX = (constraints.maxWidth * state.scale) - constraints.maxWidth
//                val maxY = (constraints.maxHeight * state.scale) - constraints.maxHeight
//
//                state._offsetX.updateBounds(
//                    lowerBound = (-maxX / 2) * 1.3f,
//                    upperBound = (maxX / 2) * 1.3f
//                )
//
//                state._offsetY.updateBounds(
//                    lowerBound = (-maxY / 2),
//                    upperBound = (maxY / 2)
//                )
//
//                coroutineScope.launch {
//                    launch {
//                        state._offsetX.animateDecay(velocity.x, decay)
//                    }
//                    launch {
//                        state._offsetY.animateDecay(velocity.y, decay)
//                    }
//                }
//
//                change.consume()
//            }
//        }
//        .graphicsLayer {
//            scaleX = state.scale
//            scaleY = state.scale
//            translationX = state._offsetX.value
//            translationY = state._offsetY.value
//        }
//}
//
//fun Modifier.tapToZoomHorizontal(
//    state: HorizontalPdfReaderState,
//    constraints: Constraints
//): Modifier = composed(
//    inspectorInfo = debugInspectorInfo {
//        name = "horizontalTapToZoom"
//        properties["state"] = state
//    }
//) {
//    val coroutineScope = rememberCoroutineScope()
//    this
//        .pointerInput(Unit) {
//            detectTapGestures(
//                onDoubleTap = { tapCenter ->
//                    if (!state.zoomEnabled) return@detectTapGestures
//                    if (state._scale.value > 1.0f) {
//                        coroutineScope.launch {
//                            state._scale.animateTo(1f)
//
//                            state.animateOffset(0f, 0f)
//                        }
//                    } else {
//                        coroutineScope.launch {
//                            state._scale.animateTo(3f)
//
//                            val center = Pair(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
//                            val xDiff = (tapCenter.x - center.first) * state.scale
//                            val yDiff = ((tapCenter.y - center.second) * state.scale).coerceIn(
//                                minimumValue = -(center.second * 2f),
//                                maximumValue = (center.second * 2f)
//                            )
//
//                            state.animateOffset(-xDiff, -yDiff)
//                        }
//                    }
//                }
//            )
//        }
//        .pointerInput(Unit) {
//            detectTransformGestures(true) { centroid, pan, zoom, rotation ->
//                val nOffset = if (state.scale > 1f) {
//                    val maxT = (constraints.maxWidth * state.scale) - constraints.maxWidth
//                    val maxY = (constraints.maxHeight * state.scale) - constraints.maxHeight
//                    Offset(
//                        x = (state._offsetX.value + pan.x).coerceIn(
//                            minimumValue = (-maxT / 2) * 1.3f,
//                            maximumValue = (maxT / 2) * 1.3f
//                        ),
//                        y = (state._offsetY.value + pan.y).coerceIn(
//                            minimumValue = (-maxY / 2) * 1.3f,
//                            maximumValue = (maxY / 2) * 1.3f
//                        )
//                    )
//                } else {
//                    Offset(0f, 0f)
//                }
//
//                coroutineScope.launch {
//                    state.animateOffset(nOffset)
//                }
//            }
//        }
//        .graphicsLayer {
//            scaleX = state.scale
//            scaleY = state.scale
//            translationX = state._offsetX.value
//            translationY = state._offsetY.value
//        }
//}