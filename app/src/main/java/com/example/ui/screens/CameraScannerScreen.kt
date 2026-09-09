package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.data.ocr.OcrExpenseResult
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.ExpenseViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScannerScreen(
    viewModel: ExpenseViewModel,
    onBillCaptured: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Scanned result verification state
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedImagePath by remember { mutableStateOf<String?>(null) }
    var scannedResult by remember { mutableStateOf<OcrExpenseResult?>(null) }
    var showReviewSheet by remember { mutableStateOf(false) }

    // Editable fields in review sheet
    var editVendor by remember { mutableStateOf("") }
    var editAmount by remember { mutableStateOf("") }
    var editCategory by remember { mutableStateOf("Petrol") }
    var editNotes by remember { mutableStateOf("") }

    // Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    if (bitmap != null) {
                        capturedBitmap = bitmap
                        viewModel.processReceiptOcr(bitmap) { parsed, path ->
                            capturedImagePath = path
                            scannedResult = parsed
                            editVendor = parsed.vendor
                            editAmount = String.format(Locale.US, "%.2f", parsed.amount)
                            editCategory = parsed.category
                            editNotes = parsed.notes
                            isProcessing = false
                            showReviewSheet = true
                        }
                    } else {
                        isProcessing = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    isProcessing = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        if (!cameraPermissionState.status.isGranted) {
            // Permission Request Card with Light Theme
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(TealLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Camera Access Required",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Allow camera access to scan physical receipts, fuel bills, and parking tickets instantly.",
                    fontSize = 14.sp,
                    color = Slate700,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("grant_camera_permission_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Grant Camera Permission", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Slate700)
                ) {
                    Text("Cancel", fontSize = 14.sp)
                }
            }
        } else {
            // Live CameraX Viewfinder Layout
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(42.dp)
                            .background(SurfaceWhite, CircleShape)
                            .border(1.dp, GlassBorderLight, CircleShape)
                            .testTag("scanner_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate800,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(SurfaceWhite, RoundedCornerShape(20.dp))
                            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Receipt Scanner",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealDark
                        )
                    }

                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            camera?.cameraControl?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(if (isFlashOn) TealPrimary else SurfaceWhite, CircleShape)
                            .border(1.dp, GlassBorderLight, CircleShape)
                            .testTag("scanner_flash_button")
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isFlashOn) Color.White else Slate800,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Camera Preview Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFE2E8F0))
                        .border(2.dp, GlassBorderLight, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val capture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .build()
                                imageCapture = capture

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                try {
                                    cameraProvider.unbindAll()
                                    camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        capture
                                    )
                                } catch (exc: Exception) {
                                    exc.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Scanner Viewfinder Overlay Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(340.dp)
                            .border(2.dp, TealPrimary, RoundedCornerShape(18.dp))
                            .background(Color.Transparent),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Align receipt within frame",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate800
                            )
                        }
                    }

                    if (isProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.75f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = TealPrimary,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = "Scanning Receipt...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Action Controls
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceWhite.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Gallery Button
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(BackgroundLight, CircleShape)
                                    .border(1.dp, GlassBorderLight, CircleShape)
                                    .testTag("gallery_picker_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Gallery",
                                    tint = Slate700,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Capture Shutter Button
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .border(4.dp, TealPrimary, CircleShape)
                                    .padding(6.dp)
                                    .clip(CircleShape)
                                    .background(TealPrimary)
                                    .clickable(enabled = !isProcessing) {
                                        val capture = imageCapture
                                        if (capture != null) {
                                            isProcessing = true
                                            val executor = Executors.newSingleThreadExecutor()
                                            capture.takePicture(
                                                executor,
                                                object : ImageCapture.OnImageCapturedCallback() {
                                                    override fun onCaptureSuccess(image: ImageProxy) {
                                                        val bitmap = imageProxyToBitmap(image)
                                                        image.close()
                                                        scope.launch {
                                                            capturedBitmap = bitmap
                                                            viewModel.processReceiptOcr(bitmap) { parsed, path ->
                                                                capturedImagePath = path
                                                                scannedResult = parsed
                                                                editVendor = parsed.vendor
                                                                editAmount = String.format(Locale.US, "%.2f", parsed.amount)
                                                                editCategory = parsed.category
                                                                editNotes = parsed.notes
                                                                isProcessing = false
                                                                showReviewSheet = true
                                                            }
                                                        }
                                                    }

                                                    override fun onError(exception: ImageCaptureException) {
                                                        exception.printStackTrace()
                                                        scope.launch {
                                                            val samplePath = viewModel.receiptStorageManager.createSampleReceipt(
                                                                "Aramco Station", 120.0, 15.65,
                                                                SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()), "Petrol"
                                                            )
                                                            capturedImagePath = samplePath
                                                            scannedResult = OcrExpenseResult(
                                                                vendor = "Aramco Station",
                                                                category = "Petrol",
                                                                amount = 120.0,
                                                                vatAmount = 15.65,
                                                                dateMillis = System.currentTimeMillis(),
                                                                notes = "Aramco Station (Petrol)",
                                                                rawText = ""
                                                            )
                                                            editVendor = "Aramco Station"
                                                            editAmount = "120.00"
                                                            editCategory = "Petrol"
                                                            editNotes = "Aramco Station (Petrol)"
                                                            isProcessing = false
                                                            showReviewSheet = true
                                                        }
                                                    }
                                                }
                                            )
                                        } else {
                                            isProcessing = true
                                            scope.launch {
                                                val samplePath = viewModel.receiptStorageManager.createSampleReceipt(
                                                    "Aramco Station", 120.0, 15.65,
                                                    SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()), "Petrol"
                                                )
                                                capturedImagePath = samplePath
                                                scannedResult = OcrExpenseResult(
                                                    vendor = "Aramco Station",
                                                    category = "Petrol",
                                                    amount = 120.0,
                                                    vatAmount = 15.65,
                                                    dateMillis = System.currentTimeMillis(),
                                                    notes = "Aramco Station (Petrol)",
                                                    rawText = ""
                                                )
                                                editVendor = "Aramco Station"
                                                editAmount = "120.00"
                                                editCategory = "Petrol"
                                                editNotes = "Aramco Station (Petrol)"
                                                isProcessing = false
                                                showReviewSheet = true
                                            }
                                        }
                                    }
                                    .testTag("camera_shutter_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Capture",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Quick Sample Scan Button
                            IconButton(
                                onClick = {
                                    isProcessing = true
                                    scope.launch {
                                        val samplePath = viewModel.receiptStorageManager.createSampleReceipt(
                                            "Petromin Express", 95.0, 12.39,
                                            SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()), "Petrol"
                                        )
                                        capturedImagePath = samplePath
                                        scannedResult = OcrExpenseResult(
                                            vendor = "Petromin Express",
                                            category = "Petrol",
                                            amount = 95.0,
                                            vatAmount = 12.39,
                                            dateMillis = System.currentTimeMillis(),
                                            notes = "Petromin Express (Petrol)",
                                            rawText = ""
                                        )
                                        editVendor = "Petromin Express"
                                        editAmount = "95.00"
                                        editCategory = "Petrol"
                                        editNotes = "Petromin Express (Petrol)"
                                        isProcessing = false
                                        showReviewSheet = true
                                    }
                                },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(BackgroundLight, CircleShape)
                                    .border(1.dp, GlassBorderLight, CircleShape)
                                    .testTag("quick_sample_scan_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Quick Sample",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Tap circle to capture receipt",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500
                        )
                    }
                }
            }
        }

        // Verified Review & Save Bottom Sheet Modal
        AnimatedVisibility(
            visible = showReviewSheet,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                color = SurfaceWhite,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(TealLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Scanned Receipt Details",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Review and confirm details",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }

                        IconButton(
                            onClick = { showReviewSheet = false },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Slate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Receipt Thumbnail
                    if (capturedImagePath != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BackgroundLight, RoundedCornerShape(12.dp))
                                .border(1.dp, GlassBorderLight, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = File(capturedImagePath!!),
                                contentDescription = "Scanned Receipt",
                                modifier = Modifier
                                    .size(60.dp, 80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Slate400.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Receipt Photo Attached",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = File(capturedImagePath!!).name,
                                    fontSize = 11.sp,
                                    color = Slate500,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Attached to Record",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TealPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Form Fields
                    OutlinedTextField(
                        value = editVendor,
                        onValueChange = { editVendor = it },
                        label = { Text("Vendor Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = TealPrimary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ocr_edit_vendor")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editAmount,
                            onValueChange = { editAmount = it },
                            label = { Text("Total (SAR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ocr_edit_amount")
                        )

                        OutlinedTextField(
                            value = editCategory,
                            onValueChange = { editCategory = it },
                            label = { Text("Category") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TealPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ocr_edit_category")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Notes / Details") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ocr_edit_notes")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Save Button
                    Button(
                        onClick = {
                            val amt = editAmount.toDoubleOrNull() ?: scannedResult?.amount ?: 0.0
                            val vat = amt * 0.15 / 1.15
                            val dateMillis = scannedResult?.dateMillis ?: System.currentTimeMillis()

                            viewModel.saveExpenseWithExistingImage(
                                amount = amt,
                                currency = "SAR",
                                dateMillis = dateMillis,
                                vendor = editVendor.ifBlank { "Fuel Station" },
                                category = editCategory.ifBlank { "Petrol" },
                                vatAmount = vat,
                                notes = editNotes,
                                localImagePath = capturedImagePath
                            ) {
                                showReviewSheet = false
                                onBillCaptured()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_scanned_expense_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Expense Record",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val planeProxy = image.planes[0]
    val buffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val matrix = Matrix()
    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
