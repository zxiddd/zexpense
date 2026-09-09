package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.pdf.MonthlyPdfGenerator
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.TealBorder
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
fun PdfPreviewScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthExpenses by viewModel.currentMonthExpenses.collectAsState()

    val totalAmount = monthExpenses.sumOf { it.amount }
    val totalBills = monthExpenses.size

    val pdfGenerator = remember { MonthlyPdfGenerator(context) }
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)

    val expensesWithImages = monthExpenses.filter { !it.localImagePath.isNullOrBlank() }

    fun generateAndShare() {
        val file = pdfGenerator.generateMonthlyPdf(
            employeeName = userProfile.name,
            monthName = selectedMonth.label.split(" ").firstOrNull() ?: "September",
            yearStr = selectedMonth.year.toString(),
            expenses = monthExpenses
        )
        file?.let { pdfGenerator.sharePdf(it) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
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
                    Text(
                        text = "Preview PDF",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }

                IconButton(
                    onClick = { generateAndShare() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TealPrimary
                    )
                }
            }
        }

        // PDF Sheet Preview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pdf_sheet_preview"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceLight),
                border = BorderStroke(1.dp, GlassBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Document Header
                    Text(
                        text = "MONTHLY EXPENSE CLAIM",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TealPrimary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Employee: ${userProfile.name}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )
                        Text(
                            text = "Month: ${selectedMonth.label}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Structured Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TealLight.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Date", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.2f))
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.2f))
                        Text("Amount (SAR)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Table rows (first 6)
                    val displayList = monthExpenses.take(6)
                    displayList.forEach { exp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormatter.format(Date(exp.dateMillis)),
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = exp.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMain,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f", exp.amount),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(TealBorder.copy(alpha = 0.3f)))
                    }

                    if (monthExpenses.size > 6) {
                        Text(
                            text = "• • • and ${monthExpenses.size - 6} more bills in appendix",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Document Total & Number of Bills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TealLight, RoundedCornerShape(10.dp))
                            .border(1.dp, TealBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = String.format(Locale.US, "%,.2f SAR", totalAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TealPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "NUMBER OF BILLS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = "$totalBills",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextMain
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Attached Bills Section
                    Text(
                        text = "ATTACHED BILLS ($totalBills)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val thumbnails = expensesWithImages.take(4)
                        thumbnails.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, TealBorder, RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(item.localImagePath!!))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Bill Thumbnail",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        if (expensesWithImages.size > 4) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(TealLight, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${expensesWithImages.size - 4}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Button: Export / Share PDF
        item {
            Button(
                onClick = { generateAndShare() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_export_pdf_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export PDF",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
