/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.vmusic.takeserver.ui.screens.musicshare

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.vmusic.takeserver.R
import com.vmusic.takeserver.ui.component.DefaultDialog
import com.vmusic.takeserver.utils.ComposeToImage
import com.vmusic.takeserver.utils.SubjectSegmentationProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomMusicShareScreen(
    navController: NavController,
    song: ShareSongInfo,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var state by remember { mutableStateOf(MusicShareState()) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var foregroundBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var artBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var accent by remember { mutableIntStateOf(0xFF3A2E55.toInt()) }
    var exporting by remember { mutableStateOf(false) }
    var segmenting by remember { mutableStateOf(false) }
    var showDiscard by remember { mutableStateOf(false) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            try {
                photoBitmap?.recycle()
                foregroundBitmap?.recycle()
                artBitmap?.recycle()
            } catch (_: Exception) {}
        }
    }

    val pickPhoto =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            photoUri = uri
        }

    LaunchedEffect(photoUri) {
        photoBitmap?.recycle()
        foregroundBitmap?.recycle()
        photoBitmap = photoUri?.let { loadBitmap(context, it, 1600) }
    }

    LaunchedEffect(photoBitmap) {
        if (photoBitmap != null) {
            segmenting = true
            val (fg, _) = SubjectSegmentationProcessor.segment(photoBitmap!!)
            foregroundBitmap = fg
            segmenting = false
        } else {
            foregroundBitmap = null
        }
    }

    LaunchedEffect(song.thumbnailUrl) {
        artBitmap = song.thumbnailUrl?.let { loadBitmap(context, it, 512) }
    }
    LaunchedEffect(artBitmap, photoBitmap) {
        accent = withContext(Dispatchers.Default) { MusicShareRenderer.accentColor(artBitmap, photoBitmap) }
    }

    val placeholder = stringResource(R.string.music_share_choose_photo)
    val shareLabel = stringResource(R.string.share)
    val savedLabel = stringResource(R.string.music_share_saved)
    val failedLabel = stringResource(R.string.music_share_failed)

    suspend fun export(shouldShare: Boolean) {
        exporting = true
        try {
            val bitmap =
                withContext(Dispatchers.Default) {
                    val result =
                        Bitmap.createBitmap(
                            MusicShareRenderer.EXPORT_WIDTH,
                            MusicShareRenderer.EXPORT_HEIGHT,
                            Bitmap.Config.ARGB_8888,
                        )
                    MusicShareRenderer.draw(
                        Canvas(result),
                        MusicShareRenderer.EXPORT_WIDTH,
                        MusicShareRenderer.EXPORT_HEIGHT,
                        song,
                        photoBitmap,
                        foregroundBitmap,
                        artBitmap,
                        state,
                        accent,
                        placeholder,
                    )
                    result
                }
            val uri =
                withContext(Dispatchers.IO) {
                    ComposeToImage.saveBitmapAsFile(context, bitmap, "music_share_${song.id}_${System.currentTimeMillis()}")
                }
            if (shouldShare) {
                val intent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                context.startActivity(Intent.createChooser(intent, shareLabel))
            } else {
                snackbarHostState.showSnackbar(savedLabel)
            }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("$failedLabel: ${e.message}")
        } finally {
            exporting = false
        }
    }

    fun requestBack() {
        if (photoUri != null || state != MusicShareState()) {
            showDiscard = true
        } else {
            navController.navigateUp()
        }
    }

    BackHandler { requestBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.custom_music_share)) },
                navigationIcon = {
                    IconButton(onClick = { requestBack() }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    if (exporting || segmenting) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                AndroidView(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(MusicShareRenderer.EXPORT_WIDTH / MusicShareRenderer.EXPORT_HEIGHT.toFloat())
                            .clip(RoundedCornerShape(20.dp)),
                    factory = { MusicShareCanvasView(it) },
                    update = { view ->
                        view.song = song
                        view.photo = photoBitmap
                        view.foreground = foregroundBitmap
                        view.art = artBitmap
                        view.state = state
                        view.accent = accent
                        view.placeholder = placeholder
                        view.onStateChange = { state = it }
                        view.invalidate()
                    },
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedButton(
                    onClick = {
                        pickPhoto.launch(
                            PickVisualMediaRequest(
                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (photoUri == null) {
                            stringResource(R.string.music_share_choose_photo)
                        } else {
                            stringResource(R.string.music_share_change_photo)
                        },
                    )
                }

                if (segmenting) {
                    Text(
                        text = "Running ML Kit Selfie Segmentation cutout...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                SectionLabel("Card Position")
                Row {
                    MusicPlacement.entries.forEach { placement ->
                        FilterChip(
                            selected = state.placement == placement,
                            onClick = { state = state.copy(placement = placement) },
                            label = {
                                Text(
                                    when (placement) {
                                        MusicPlacement.LEFT -> "Left"
                                        MusicPlacement.CENTER -> "Center"
                                        MusicPlacement.RIGHT -> "Right"
                                    },
                                )
                            },
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                }

                SectionLabel("3D Floating Music Player")
                SliderRow("Size", state.musicScale, 0.6f..1.6f) {
                    state = state.copy(musicScale = it)
                }
                SliderRow("Rotation", state.musicRotation, -30f..30f) {
                    state = state.copy(musicRotation = it)
                }
                SliderRow("3D Perspective Skew", state.musicSkew, -0.3f..0.3f) {
                    state = state.copy(musicSkew = it)
                }
                SliderRow("Opacity", state.musicAlpha, 0.2f..1f) {
                    state = state.copy(musicAlpha = it)
                }

                SectionLabel("Background Photo")
                SliderRow("Photo Scale", state.photoScale, 0.8f..2.0f) {
                    state = state.copy(photoScale = it)
                }
                SliderRow("Photo Opacity", state.photoAlpha, 0.2f..1f) {
                    state = state.copy(photoAlpha = it)
                }

                TextButton(onClick = { state = MusicShareState() }) {
                    Text(stringResource(R.string.music_share_reset))
                }

                Row {
                    Button(
                        onClick = { scope.launch { export(false) } },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                    ) {
                        Text(stringResource(R.string.music_share_save))
                    }
                    Button(
                        onClick = { scope.launch { export(true) } },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                    ) {
                        Text(shareLabel)
                    }
                }
            }
        }
    }

    if (showDiscard) {
        DefaultDialog(
            onDismiss = { showDiscard = false },
            title = { Text(stringResource(R.string.music_share_discard_title)) },
            buttons = {
                TextButton(onClick = { showDiscard = false }) {
                    Text(stringResource(R.string.music_share_keep_editing))
                }
                TextButton(onClick = { navController.navigateUp() }) {
                    Text(stringResource(R.string.music_share_discard))
                }
            },
        ) {
            Text(stringResource(R.string.music_share_discard_message))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Text(
        text = "$label ${(value * 100).roundToInt()}",
        style = MaterialTheme.typography.labelMedium,
    )
    Slider(value = value, valueRange = range, onValueChange = onChange)
}

private suspend fun loadBitmap(
    context: Context,
    data: Any,
    size: Int,
): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            ImageLoader(context)
                .execute(
                    ImageRequest
                        .Builder(context)
                        .data(data)
                        .size(size)
                        .allowHardware(false)
                        .build(),
                ).image
                ?.toBitmap()
        } catch (_: Exception) {
            null
        }
    }
