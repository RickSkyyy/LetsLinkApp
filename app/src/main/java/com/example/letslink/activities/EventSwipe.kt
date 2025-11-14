package com.example.letslink.activities

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
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
import coil.compose.AsyncImage
import com.example.letslink.R
import com.example.letslink.model.Event
import com.example.letslink.model.EventVoting_m
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@Composable
fun EventVotingScreen(events:List<EventVoting_m>, groupId: String, userId: String) {
        val context = LocalContext.current

    Log.d("EventVotingScreen", "Events: $events")
    EventSwipeDeck(
        events = events,
        onSwipeLeft ={ event ->
            saveVote(groupId, event.eventId, userId,"dislike")
            Toast.makeText(context, "You disliked ${event.title}", Toast.LENGTH_SHORT).show()
        },
        onSwipeRight = { event ->
            saveVote(groupId, event.eventId, userId,"like")
            Toast.makeText(context, "You liked ${event.title}", Toast.LENGTH_SHORT).show()
        }
    )
}

fun saveVote(groupId: String, eventId: String, userId: String, vote: String) {
    val dbRef = FirebaseDatabase.getInstance()
        .getReference("group_voting")
        .child(groupId)
        .child("events")
        .child(eventId)
        .child("votes")
        .child(userId)
    dbRef.setValue(vote)
}

@Composable
fun EventCard(event: EventVoting_m, modifier: Modifier = Modifier) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // --- Background image only if URL exists ---
            if (event.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Slight dark overlay to improve text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )
            }

            // --- Foreground (text) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = event.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (event.imageUrl.isNotEmpty()) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    fontSize = 16.sp,
                    color = if (event.imageUrl.isNotEmpty()) Color.White else Color.Black
                )
            }
        }
    }
}

@Composable
fun EventSwipeDeck(
    events: List<EventVoting_m>,
    onSwipeLeft: (EventVoting_m) -> Unit,
    onSwipeRight: (EventVoting_m) -> Unit
) {
    var topIndex by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val dragOffset = remember { mutableStateOf(0f) }
    val offsetX = remember { Animatable(0f) }
    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels.toFloat()
    val context = LocalContext.current
    Crossfade(targetState = topIndex >= events.size) { done ->
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
                            (context as? Activity)?.finish()
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
                events.asReversed().forEachIndexed { index, event ->
                    if (index < events.size - topIndex) {
                        val isTop = index == events.size - topIndex - 1
                        EventCard(
                            event = event,
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
                        onSwipeRight(events[topIndex])
                        topIndex++
                        dragOffset.value = 0f
                        offsetX.snapTo(0f)
                    }
                } else if (dragOffset.value < -300) {
                    scope.launch {
                        offsetX.animateTo(-screenWidth, animationSpec = tween(300))
                        onSwipeLeft(events[topIndex])
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
