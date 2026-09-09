package com.example.ui.screens

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryIconBadge
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealBorder
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var amountText by remember { mutableStateOf("") }
    var vendorText by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("Petrol") }
    var vatText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isSmartExtracted by remember { mutableStateOf(false) }

    var showCategoryDropdown by remember { mutableStateOf(false) }

    val categories = listOf("Petrol", "Parking", "Other", "Maintenance", "Food", "Supplies")
    val quickVendors = listOf("Aramco", "Petromin", "SASCO", "Shell", "ADNOC", "Jeddah")

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)

    // Gallery Picker Contract
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                selectedBitmap = bitmap
                // Run Smart OCR / Heuristics
                viewModel.processReceiptOcr(bitmap) { extracted, _ ->
                    vendorText = extracted.vendor
                    categoryText = extracted.category
                    amountText = if (extracted.amount > 0) String.format(Locale.US, "%.2f", extracted.amount) else ""
                    vatText = if (extracted.vatAmount > 0) String.format(Locale.US, "%.2f", extracted.vatAmount) else ""
                    if (extracted.notes.isNotBlank()) notesText = extracted.notes
                    isSmartExtracted = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Date Picker Dialog
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val newCal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            dateMillis = newCal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextMain
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "NEW BILL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = TealPrimary
                    )
                    Text(
                        text = "Add Expense",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }
            }
        }

        // Receipt Upload / Camera Area
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                border = BorderStroke(1.dp, Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (selectedBitmap != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Receipt",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Top Smart OCR Badge
                        if (isSmartExtracted) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xDD0F172A),
                                modifier = Modifier
                                    .padding(12.dp)
                                    .align(Alignment.TopStart)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Smart Extracted",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Retake / Remove button
                        IconButton(
                            onClick = {
                                selectedBitmap = null
                                isSmartExtracted = false
                            },
                            modifier = Modifier
                                .padding(12.dp)
                                .size(36.dp)
                                .background(Color(0xAA000000), CircleShape)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(TealLight, CircleShape)
                                .border(1.dp, TealBorder, CircleShape)
                                .clickable { onNavigateToCamera() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                tint = TealPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Take a photo",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "or ",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "upload from gallery",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TealPrimary,
                                modifier = Modifier
                                    .clickable { galleryLauncher.launch("image/*") }
                                    .testTag("upload_gallery_button")
                            )
                        }
                    }
                }
            }
        }

        // Section Title: Extracted Details
        item {
            Text(
                text = "Extracted Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        }

        // Form: Date Field
        item {
            Column {
                Text(
                    text = "Date",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { datePickerDialog.show() }
                        .testTag("expense_date_picker")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateFormatter.format(Date(dateMillis)),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMain
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Pick Date",
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Form: Category Selector Dropdown
        item {
            Column {
                Text(
                    text = "Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { showCategoryDropdown = true }
                            .testTag("category_dropdown")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CategoryIconBadge(category = categoryText, size = 28)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = categoryText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextMain
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Category",
                                tint = TealPrimary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryIconBadge(category = cat, size = 26)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cat, color = TextMain)
                                    }
                                },
                                onClick = {
                                    categoryText = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Form: Amount
        item {
            Column {
                Text(
                    text = "Amount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        val d = it.toDoubleOrNull() ?: 0.0
                        vatText = String.format(Locale.US, "%.2f", d * 0.15 / 1.15)
                    },
                    placeholder = { Text("0.00", color = Slate400) },
                    trailingIcon = {
                        Text(
                            text = "SAR",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.75f),
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input")
                )
            }
        }

        // Form: Vendor / Station + Quick Chips
        item {
            Column {
                Text(
                    text = "Vendor / Station",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = vendorText,
                    onValueChange = { vendorText = it },
                    placeholder = { Text("e.g. Aramco, SASCO", color = Slate400) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.75f),
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vendor_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickVendors.take(4).forEach { v ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (vendorText.equals(v, ignoreCase = true)) TealLight else Color.White.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, if (vendorText.equals(v, ignoreCase = true)) TealBorder else Color.White),
                            modifier = Modifier.clickable { vendorText = v }
                        ) {
                            Text(
                                text = v,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (vendorText.equals(v, ignoreCase = true)) TealPrimary else TextMain,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Form: Notes (Optional)
        item {
            Column {
                Text(
                    text = "Notes (optional)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("Add a note", color = Slate400) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.75f),
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color.White
                    ),
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_input")
                )
            }
        }

        // Submit Button
        item {
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val vat = vatText.toDoubleOrNull() ?: (amt * 0.15 / 1.15)
                    val vendor = vendorText.ifBlank { "Aramco Station" }

                    viewModel.saveExpense(
                        amount = amt,
                        currency = "SAR",
                        dateMillis = dateMillis,
                        vendor = vendor,
                        category = categoryText,
                        vatAmount = vat,
                        notes = notesText,
                        bitmap = selectedBitmap,
                        onComplete = {
                            onNavigateBack()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_expense_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                enabled = amountText.isNotBlank() || selectedBitmap != null
            ) {
                Text(
                    text = "Save Expense",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

