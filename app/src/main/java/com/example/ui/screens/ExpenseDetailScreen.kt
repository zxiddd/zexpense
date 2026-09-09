package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.ExpenseEntity
import com.example.ui.components.CategoryIconBadge
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.RoseError
import com.example.ui.theme.RoseLight
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealBorder
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ExpenseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allExpenses by viewModel.rawExpenses.collectAsState()
    val expense = allExpenses.find { it.id == expenseId }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showFullscreenReceipt by remember { mutableStateOf(false) }

    if (expense == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Expense not found", color = TextMuted)
        }
        return
    }

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val formattedDate = dateFormatter.format(Date(expense.dateMillis))
    val hasImage = !expense.localImagePath.isNullOrBlank() && File(expense.localImagePath).exists()

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "RECEIPT VIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = TealPrimary
                        )
                        Text(
                            text = "Expense Detail",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TealLight,
                    border = BorderStroke(1.dp, TealBorder),
                    modifier = Modifier
                        .clickable { showEditDialog = true }
                        .testTag("edit_expense_button")
                ) {
                    Text(
                        text = "Edit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Receipt Large Image Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceLight),
                border = BorderStroke(1.dp, GlassBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (hasImage) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(expense.localImagePath!!))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Scanned Receipt",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        // Fullscreen / Zoom button
                        IconButton(
                            onClick = { showFullscreenReceipt = true },
                            modifier = Modifier
                                .padding(12.dp)
                                .size(40.dp)
                                .background(Color(0x99000000), CircleShape)
                                .align(Alignment.BottomEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Expand",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
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
                        CategoryIconBadge(category = expense.category, size = 64)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No receipt image attached",
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Info List Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceLight),
                border = BorderStroke(1.dp, GlassBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Date
                    DetailRow(
                        label = "Date",
                        value = formattedDate,
                        icon = Icons.Default.CalendarMonth
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1A006A60)))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Category", fontSize = 14.sp, color = TextMuted)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIconBadge(category = expense.category, size = 26)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = expense.category,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMain
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1A006A60)))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Vendor / Station
                    DetailRow(
                        label = "Vendor / Station",
                        value = expense.vendor
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1A006A60)))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Amount", fontSize = 14.sp, color = TextMuted)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.2f", expense.amount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TealPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = expense.currency,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                modifier = Modifier.padding(bottom = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1A006A60)))
                    Spacer(modifier = Modifier.height(14.dp))

                    // VAT Amount
                    DetailRow(
                        label = "VAT (15%)",
                        value = "${String.format(Locale.US, "%.2f", expense.vatAmount)} SAR"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1A006A60)))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Storage Location
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Storage", fontSize = 14.sp, color = TextMuted)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Secure Storage",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0x1A006A60)))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Notes
                    DetailRow(
                        label = "Notes",
                        value = expense.notes.ifBlank { "—" }
                    )
                }
            }
        }

        // Privacy Guarantee Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TealLight),
                border = BorderStroke(1.dp, TealBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Private Document: Saved securely and accessible anytime.",
                        fontSize = 12.sp,
                        color = TealDark,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Bottom Action Buttons: Share & Delete
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    onClick = {
                        shareExpense(context, expense)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("share_expense_button"),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, GlassBorderLight)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                    }
                }

                Surface(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("delete_expense_button"),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, RoseLight)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = RoseError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = RoseError)
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense?", fontWeight = FontWeight.Bold, color = TextMain) },
            text = { Text("Are you sure you want to remove this expense and its local receipt? This action cannot be undone.", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteExpense(expense) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Edit Expense Dialog
    if (showEditDialog) {
        EditExpenseDialog(
            expense = expense,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                viewModel.updateExpense(updated) {
                    showEditDialog = false
                }
            }
        )
    }

    // Fullscreen Receipt Modal
    if (showFullscreenReceipt && hasImage) {
        Dialog(onDismissRequest = { showFullscreenReceipt = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(expense.localImagePath!!))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Zoomed Receipt",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    IconButton(
                        onClick = { showFullscreenReceipt = false },
                        modifier = Modifier
                            .padding(12.dp)
                            .size(36.dp)
                            .background(Color(0x99000000), CircleShape)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextMuted
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EditExpenseDialog(
    expense: ExpenseEntity,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var vendor by remember { mutableStateOf(expense.vendor) }
    var notes by remember { mutableStateOf(expense.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense Details", fontWeight = FontWeight.Bold, color = TextMain) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (SAR)") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = TealBorder
                    )
                )
                OutlinedTextField(
                    value = vendor,
                    onValueChange = { vendor = it },
                    label = { Text("Vendor / Station") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = TealBorder
                    )
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = TealBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: expense.amount
                    val vat = amt * 0.15 / 1.15
                    onSave(
                        expense.copy(
                            amount = amt,
                            vendor = vendor,
                            vatAmount = vat,
                            notes = notes
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

private fun shareExpense(context: android.content.Context, expense: ExpenseEntity) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val text = "🧾 *ZExpense Bill Details*\n" +
            "• Vendor: ${expense.vendor}\n" +
            "• Category: ${expense.category}\n" +
            "• Amount: ${String.format(Locale.US, "%.2f SAR", expense.amount)}\n" +
            "• Date: ${dateFormatter.format(Date(expense.dateMillis))}\n" +
            (if (expense.notes.isNotBlank()) "• Notes: ${expense.notes}\n" else "") +
            "\n🔒 _Private receipt stored locally on device._"

    val intent = Intent(Intent.ACTION_SEND).apply {
        if (!expense.localImagePath.isNullOrBlank() && File(expense.localImagePath).exists()) {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(expense.localImagePath)
            )
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
        }
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "Expense: ${expense.vendor}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(Intent.createChooser(intent, "Share Expense").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
