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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ExpenseListItem
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealBorder
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: ExpenseViewModel,
    onNavigateToExpenseDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthExpenses by viewModel.currentMonthExpenses.collectAsState()

    var selectedDay by remember { mutableIntStateOf(1) }

    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    // Determine days in selected month (e.g. September 2026 -> 30 days, starts on Tuesday)
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, selectedMonth.year)
        set(Calendar.MONTH, selectedMonth.monthIndex)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed Sun=0
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val expensesByDay = monthExpenses.groupBy {
        val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
        c.get(Calendar.DAY_OF_MONTH)
    }

    val selectedDayExpenses = expensesByDay[selectedDay] ?: emptyList()
    val selectedDayTotal = selectedDayExpenses.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
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
                    text = "Calendar",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            }
        }

        // Calendar Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calendar_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                border = BorderStroke(1.dp, Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Month title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedMonth.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Days of week header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEach { d ->
                            Text(
                                text = d,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days Grid
                    val totalSlots = firstDayOfWeek + maxDays
                    val rows = (totalSlots + 6) / 7

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (c in 0 until 7) {
                                val slotIndex = r * 7 + c
                                val dayNum = slotIndex - firstDayOfWeek + 1

                                if (dayNum in 1..maxDays) {
                                    val isSelected = (dayNum == selectedDay)
                                    val hasExpenses = expensesByDay.containsKey(dayNum)

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) TealPrimary else if (hasExpenses) TealLight else Color.Transparent
                                            )
                                            .clickable { selectedDay = dayNum },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || hasExpenses) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else if (hasExpenses) TealPrimary else TextMain
                                            )
                                            if (hasExpenses && !isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(TealPrimary, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$selectedDay ${selectedMonth.label.split(" ").firstOrNull() ?: ""}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )

                if (selectedDayExpenses.isNotEmpty()) {
                    Text(
                        text = "Total: ${String.format(Locale.US, "%.2f SAR", selectedDayTotal)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }
        }

        // Expenses on that day
        if (selectedDayExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses recorded on this day.",
                            fontSize = 13.sp,
                            color = Slate400
                        )
                    }
                }
            }
        } else {
            items(selectedDayExpenses, key = { it.id }) { exp ->
                ExpenseListItem(
                    expense = exp,
                    onClick = { onNavigateToExpenseDetail(exp.id) }
                )
            }
        }
    }
}

