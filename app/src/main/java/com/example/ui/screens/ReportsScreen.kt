package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.ExpenseCsvExporter
import com.example.data.pdf.MonthlyPdfGenerator
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate100
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
import com.example.ui.viewmodel.MonthOption
import java.util.Calendar
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ExpenseViewModel,
    onNavigateToPdfPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthExpenses by viewModel.currentMonthExpenses.collectAsState()

    val totalAmount = monthExpenses.sumOf { it.amount }
    val totalBills = monthExpenses.size
    val categoriesCount = monthExpenses.map { it.category }.distinct().size
    val avgPerDay = if (totalBills > 0) totalAmount / 30.0 else 0.0

    val monthsList = listOf(
        MonthOption("September 2026", Calendar.SEPTEMBER, 2026),
        MonthOption("August 2026", Calendar.AUGUST, 2026),
        MonthOption("July 2026", Calendar.JULY, 2026),
        MonthOption("October 2026", Calendar.OCTOBER, 2026)
    )
    var showMonthDropdown by remember { mutableStateOf(false) }

    val csvExporter = remember { ExpenseCsvExporter(context) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Screen Title
        item {
            Column {
                Text(
                    text = "ZEXPENSE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = TealPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Reports & Analytics",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            }
        }

        // Month Selector
        item {
            Box {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier
                        .clickable { showMonthDropdown = true }
                        .testTag("reports_month_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedMonth.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Month",
                            tint = TealPrimary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMonthDropdown,
                    onDismissRequest = { showMonthDropdown = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    monthsList.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m.label, fontWeight = if (m == selectedMonth) FontWeight.Bold else FontWeight.Normal, color = if (m == selectedMonth) TealPrimary else TextMain) },
                            onClick = {
                                viewModel.setMonth(m)
                                showMonthDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Summary Card with Wave Chart & Metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                border = BorderStroke(1.dp, Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Expenses",
                                fontSize = 13.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format(Locale.US, "%,.0f", totalAmount),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextMain
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SAR",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(TealLight, RoundedCornerShape(12.dp))
                                .border(1.dp, TealBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wave Chart Graphic
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val path = Path().apply {
                                moveTo(0f, h * 0.75f)
                                cubicTo(w * 0.22f, h * 0.25f, w * 0.42f, h * 0.8f, w * 0.65f, h * 0.35f)
                                cubicTo(w * 0.82f, h * 0.15f, w * 0.94f, h * 0.3f, w, h * 0.1f)
                            }

                            drawPath(
                                path = path,
                                color = TealPrimary,
                                style = Stroke(width = 3.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE6EFEA)))
                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Metric Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Bills
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalBills",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Text(
                                text = "Bills",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        // Categories
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$categoriesCount",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                            Text(
                                text = "Categories",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        // Avg. Per Day
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%.0f", avgPerDay),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealDark
                            )
                            Text(
                                text = "Avg. Per Day",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Export / Share Section
        item {
            Column {
                Text(
                    text = "Export / Share",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Generate a PDF report with summary and all bills.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        }

        // Big "Export as PDF" Button
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.85f),
                border = BorderStroke(1.5.dp, TealBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { onNavigateToPdfPreview() }
                    .testTag("export_pdf_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export as PDF",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }
        }

        // Other Options Section
        item {
            Text(
                text = "Other Options",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        }

        // Option: Export as Excel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val file = csvExporter.exportToCsv(
                            monthName = selectedMonth.label.split(" ").firstOrNull() ?: "September",
                            yearStr = selectedMonth.year.toString(),
                            expenses = monthExpenses
                        )
                        file?.let { csvExporter.shareCsv(it) }
                    }
                    .testTag("export_excel_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFECFDF5), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Export as Excel (CSV)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Slate400
                    )
                }
            }
        }

        // Option: Share Summary
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        csvExporter.shareTextSummary(
                            monthName = selectedMonth.label.split(" ").firstOrNull() ?: "September",
                            yearStr = selectedMonth.year.toString(),
                            expenses = monthExpenses
                        )
                    }
                    .testTag("share_summary_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(TealLight, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Share Summary",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Slate400
                    )
                }
            }
        }
    }
}

