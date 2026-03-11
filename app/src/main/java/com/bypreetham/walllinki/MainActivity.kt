package com.bypreetham.walllinki

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.bypreetham.walllinki.ui.theme.WalllinkiTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleWallpaperUpdates()
        setContent {
            WalllinkiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WallpaperBuilderScreen()
                }
            }
        }
    }

    private fun scheduleWallpaperUpdates() {
        val workRequest = PeriodicWorkRequestBuilder<WallpaperUpdateWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "WallpaperUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@Composable
fun WallpaperBuilderScreen() {
    val context = LocalContext.current
    val prefs = remember { WallpaperPreferences(context) }
    
    var timeSlots by remember { mutableStateOf(prefs.timeSlots) }
    var editingSlot by remember { mutableStateOf<TimeSlot?>(null) }
    var showAddSlot by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            editingSlot?.let { slot ->
                uris.forEach { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                val updatedSlot = slot.copy(imageUris = (slot.imageUris + uris).distinct())
                timeSlots = timeSlots.map { if (it.id == slot.id) updatedSlot else it }
                prefs.timeSlots = timeSlots
                editingSlot = updatedSlot
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF0F2F5), Color(0xFFFFFFFF))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            HeaderSection()
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                timeSlots.forEach { slot ->
                    GlassCard(
                        slot = slot,
                        onAddClick = { 
                            editingSlot = slot
                            imageLauncher.launch(arrayOf("image/*")) 
                        },
                        onRemoveImage = { uri ->
                            val updatedSlot = slot.copy(imageUris = slot.imageUris.filter { it != uri })
                            timeSlots = timeSlots.map { if (it.id == slot.id) updatedSlot else it }
                            prefs.timeSlots = timeSlots
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            AppleButton(
                text = "Apply Dynamic Wallpaper",
                onClick = {
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            ComponentName(context, DynamicWallpaperService::class.java)
                        )
                    }
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = R.drawable.ic_logo,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Walllinki",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black.copy(alpha = 0.9f)
            )
            Text(
                text = "Dynamic Harmony",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun GlassCard(
    slot: TimeSlot,
    onAddClick: () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp),
                            shadowElevation = 2.dp
                        ) {
                            val icon = when (slot.id) {
                                "morning" -> Icons.Default.LightMode
                                "evening" -> Icons.Default.Nightlight
                                else -> Icons.Default.Schedule
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp).size(20.dp),
                                tint = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(slot.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${slot.startHour}:00 - ${slot.endHour}:00", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onAddClick() },
                        color = Color.Black,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp).size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (slot.imageUris.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tap + to add images", color = Color.Black.copy(alpha = 0.3f))
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(slot.imageUris) { uri ->
                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp))) {
                                AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                Box(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(20.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape).clickable { onRemoveImage(uri) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSlotDialog(onDismiss: () -> Unit, onAdd: (String, Int, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(0) }
    var end by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Time Slot") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name (e.g. Work)") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Start Hour: $start:00")
                    Slider(value = start.toFloat(), onValueChange = { start = it.toInt() }, valueRange = 0f..23f, steps = 23)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("End Hour: $end:00")
                    Slider(value = end.toFloat(), onValueChange = { end = it.toInt() }, valueRange = 0f..23f, steps = 23)
                }
            }
        },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onAdd(name, start, end) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditSlotDialog(slot: TimeSlot, onDismiss: () -> Unit, onSave: (TimeSlot) -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(slot.name) }
    var start by remember { mutableStateOf(slot.startHour) }
    var end by remember { mutableStateOf(slot.endHour) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${slot.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Start Hour: $start:00")
                    Slider(value = start.toFloat(), onValueChange = { start = it.toInt() }, valueRange = 0f..23f, steps = 23)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("End Hour: $end:00")
                    Slider(value = end.toFloat(), onValueChange = { end = it.toInt() }, valueRange = 0f..23f, steps = 23)
                }
                Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))) {
                    Text("Delete Slot", color = Color.White)
                }
            }
        },
        confirmButton = { 
            Button(onClick = { onSave(slot.copy(name = name, startHour = start, endHour = end)) }) { 
                Text("Save") 
            } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AppleButton(text: String, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "buttonScale")
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}