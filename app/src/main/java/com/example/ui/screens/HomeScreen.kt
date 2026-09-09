package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.ExpenseListItem
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceLight
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TealBorder
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealDeepest
import com.example.ui.theme.TealGradientEnd
import com.example.ui.theme.TealGradientStart
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.MonthOption
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToExpenseDetail: (Long) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthExpenses by viewModel.currentMonthExpenses.collectAsState()

    val totalAmount = monthExpenses.sumOf { it.amount }
    val totalBills = monthExpenses.size
    val distinctCategories = monthExpenses.map { it.category }.distinct()
    val totalCategoriesCount = distinctCategories.size

    val monthsList = listOf(
        MonthOption("September 2026", Calendar.SEPTEMBER, 2026),
        MonthOption("August 2026", Calendar.AUGUST, 2026),
        MonthOption("July 2026", Calendar.JULY, 2026),
        MonthOption("October 2026", Calendar.OCTOBER, 2026)
    )
    var showMonthDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Bar: ZExpense Subhead + Dashboard Title + Frosted Avatar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = "Dashboard",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Local Storage indicator pill
                    Row(
                        modifier = Modifier
                            .background(TealLight, RoundedCornerShape(20.dp))
                            .border(1.dp, TealBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Secure Storage",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealDark
                        )
                    }

                    // User Avatar with soft mint background & teal border
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBE5E0))
                            .border(1.dp, Color(0x2B006A6A), CircleShape)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.name.take(1).uppercase(),
                            color = TealDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        // 2. Month Selector Dropdown (Frosted Glass Surface)
        item {
            Box {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, GlassBorderLight),
                    modifier = Modifier
                        .clickable { showMonthDropdown = true }
                        .testTag("month_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedMonth.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Month",
                            tint = Slate500
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMonthDropdown,
                    onDismissRequest = { showMonthDropdown = false }
                ) {
                    monthsList.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m.label) },
                            onClick = {
                                viewModel.setMonth(m)
                                showMonthDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // 3. Hero Frosted Teal Gradient Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_total_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(TealGradientStart, TealGradientEnd)
                            )
                        )
                        .padding(22.dp)
                ) {
                    // Decorative subtle blur circle
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.TopEnd)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    )

                    Column {
                        Text(
                            text = "Monthly Expenses",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "SAR ${String.format(Locale.US, "%,.2f", totalAmount)}",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Local Storage Verification Badge
                        Row(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF34D399), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "100% LOCAL & PRIVATE ROOM DB",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sub-metrics (Bills & Categories)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Bills
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "$totalBills",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Bills",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            // Categories
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Widgets,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "$totalCategoriesCount",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Categories",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Local-First Security Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TealLight),
                border = BorderStroke(1.dp, TealBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(TealPrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Local-Only Storage",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealDeepest
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Receipt photos and bill records stay 100% on your device.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 5. By Category Section (Frosted Glass Card)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassSurfaceLight),
                border = BorderStroke(1.dp, GlassBorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "By Category",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )

                        Text(
                            text = "See All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealPrimary,
                            modifier = Modifier
                                .clickable { onNavigateToCategories() }
                                .testTag("see_all_categories")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val catGroups = monthExpenses.groupBy { it.category }
                    val sortedCats = catGroups.entries.sortedByDescending { it.value.sumOf { exp -> exp.amount } }

                    if (sortedCats.isEmpty()) {
                        Text(
                            text = "No expenses recorded for this month yet.",
                            fontSize = 13.sp,
                            color = Slate400,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        sortedCats.take(3).forEach { (catName, list) ->
                            val catTotal = list.sumOf { it.amount }
                            val pct = if (totalAmount > 0) (catTotal / totalAmount).toFloat() else 0f

                            CategoryProgressItem(
                                category = catName,
                                amount = catTotal,
                                percentage = (pct * 100).toInt(),
                                progress = pct
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }
        }

        // 6. Action Button "+ Add Expense"
        item {
            Button(
                onClick = onNavigateToAddExpense,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("add_expense_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Expense / Bill",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        // 7. Recent Activity Header & Items
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT BILLS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextMuted
                )
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TealPrimary,
                    modifier = Modifier.clickable { onNavigateToExpenses() }
                )
            }
        }

        val recentExpenses = monthExpenses.take(4)
        items(recentExpenses, key = { it.id }) { expense ->
            ExpenseListItem(
                expense = expense,
                onClick = { onNavigateToExpenseDetail(expense.id) }
            )
        }
    }
}

@Composable
fun CategoryProgressItem(
    category: String,
    amount: Double,
    percentage: Int,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconBadge(category = category, size = 36)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Row {
                        Text(
                            text = String.format(Locale.US, "%,.0f", amount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "SAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val progressColor = when (category.lowercase()) {
                        "petrol" -> TealPrimary
                        "parking" -> EmeraldSuccess
                        else -> AmberWarning
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(progressColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "$percentage%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate500
                    )
                }
            }
        }
    }
}
