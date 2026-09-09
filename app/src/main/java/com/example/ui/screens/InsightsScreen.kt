package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryIconBadge
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.TealBorder
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ExpenseViewModel
import java.util.Locale

@Composable
fun InsightsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthExpenses by viewModel.currentMonthExpenses.collectAsState()

    val totalAmount = monthExpenses.sumOf { it.amount }
    val totalBills = monthExpenses.size
    val avgPerDay = if (totalBills > 0) totalAmount / 30.0 else 0.0

    val catGroups = monthExpenses.groupBy { it.category }
    val categoryColors = listOf(
        TealPrimary,
        Color(0xFF0D9488),
        Color(0xFF0284C7),
        Color(0xFF8B5CF6),
        Color(0xFFF59E0B),
        Color(0xFFEC4899)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "Insights",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
        }

        // Spending Breakdown Donut Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spending_breakdown_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceLight),
                border = BorderStroke(1.dp, GlassBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Spending Breakdown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = selectedMonth.label,
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Donut Chart
                    Box(
                        modifier = Modifier.size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 26.dp.toPx()
                            var startAngle = -90f

                            if (totalAmount == 0.0) {
                                drawArc(
                                    color = Color(0xFFE2E8F0),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                            } else {
                                var colorIdx = 0
                                catGroups.forEach { (_, list) ->
                                    val catSum = list.sumOf { it.amount }
                                    val sweep = ((catSum / totalAmount) * 360f).toFloat()
                                    val color = categoryColors[colorIdx % categoryColors.size]

                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep - 2f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweep
                                    colorIdx++
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%,.0f", totalAmount),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextMain
                            )
                            Text(
                                text = "SAR Total",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Category Legend List
                    catGroups.entries.sortedByDescending { it.value.sumOf { e -> e.amount } }.forEachIndexed { idx, (catName, list) ->
                        val catTotal = list.sumOf { it.amount }
                        val pct = if (totalAmount > 0) (catTotal / totalAmount) * 100 else 0.0
                        val color = categoryColors[idx % categoryColors.size]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color, CircleShape)
                                    )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = catName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextMain
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(Locale.US, "%.2f SAR", catTotal),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMain
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${String.format(Locale.US, "%.0f%%", pct)})",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Daily Average Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceLight),
                border = BorderStroke(1.dp, GlassBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Average",
                            fontSize = 13.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.0f", avgPerDay),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SAR / day",
                                fontSize = 12.sp,
                                color = TealPrimary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .background(TealLight, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+12% vs last mo.",
                            color = TealPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
