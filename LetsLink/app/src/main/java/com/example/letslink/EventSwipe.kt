package com.example.letslink

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun EventVotingScreen() {
    val posters = listOf(R.drawable.poster1, R.drawable.poster2, R.drawable.poster3)
    val context = LocalContext.current

    EventSwipeDeck(
        posters = posters,
        onSwipeLeft = { _ -> Toast.makeText(context, "Disliked!", Toast.LENGTH_SHORT).show() },
        onSwipeRight = { _ -> Toast.makeText(context, "Liked!", Toast.LENGTH_SHORT).show() }
    )
}

@Composable
fun EventCard(posterRes: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp)
    ) {
        Image(
            painter = painterResource(posterRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun EventSwipeDeck(
    posters: List<Int>,
    onSwipeLeft: (Int) -> Unit,
    onSwipeRight: (Int) -> Unit
) {
    var topIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dragOffset = remember { mutableStateOf(0f) }
    val offsetX = remember { Animatable(0f) }
    val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()

    Crossfade(targetState = topIndex >= posters.size) { done ->
        if (done) {
            // DONE SCREEN
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF44A22)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Done",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            (context as? android.app.Activity)?.finish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "Back To Group",
                            color = Color(0xFFF44A22),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            // SWIPE DECK
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                posters.asReversed().forEachIndexed { index, poster ->
                    if (index < posters.size - topIndex) {
                        val isTop = index == posters.size - topIndex - 1
                        EventCard(
                            posterRes = poster,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationX = if (isTop) offsetX.value + dragOffset.value else 0f
                                    rotationZ = if (isTop) (offsetX.value + dragOffset.value) / 20f else 0f
                                }
                                .pointerInput(Unit) {
                                    if (isTop) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consumeAllChanges()
                                            dragOffset.value += dragAmount.x
                                        }
                                    }
                                }
                        )
                    }
                }
            }

            LaunchedEffect(dragOffset.value) {
                if (dragOffset.value > 300) {
                    // Animate card off screen smoothly
                    scope.launch {
                        offsetX.animateTo(screenWidth, animationSpec = tween(300))
                        onSwipeRight(posters[topIndex])
                        topIndex++
                        dragOffset.value = 0f
                        offsetX.snapTo(0f)
                    }
                } else if (dragOffset.value < -300) {
                    scope.launch {
                        offsetX.animateTo(-screenWidth, animationSpec = tween(300))
                        onSwipeLeft(posters[topIndex])
                        topIndex++
                        dragOffset.value = 0f
                        offsetX.snapTo(0f)
                    }
                } else {
                    // Smoothly reset small drag
                    scope.launch { offsetX.animateTo(0f, animationSpec = tween(200)) }
                }
            }
        }
    }
}
