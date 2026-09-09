package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.ExpenseListItem
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TextMain
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesListScreen(
    viewModel: ExpenseViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToExpenseDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterScope by viewModel.filterScope.collectAsState()
    val selectedCategory by viewModel.selectedFilterCategory.collectAsState()
    val filteredExpenses by viewModel.filteredExpenses.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val totalAmount = filteredExpenses.sumOf { it.amount }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        text = "Expenses",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }
            }

            // Search Bar + Filter Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search expenses...",
                                color = Slate400,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Slate400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.85f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.75f),
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("search_expenses_input")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selectedCategory != null && selectedCategory != "All") TealPrimary else Color.White.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, if (selectedCategory != null && selectedCategory != "All") TealPrimary else Color.White),
                        modifier = Modifier
                            .size(52.dp)
                            .clickable { showFilterSheet = true }
                            .testTag("filter_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (selectedCategory != null && selectedCategory != "All") Color.White else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Segmented Pill Scope: "This Month" / "All"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEAF1EE), RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    val isThisMonth = filterScope == "This Month"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .background(
                                color = if (isThisMonth) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(11.dp)
                            )
                            .clickable { viewModel.setFilterScope("This Month") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "This Month",
                            fontSize = 14.sp,
                            fontWeight = if (isThisMonth) FontWeight.Bold else FontWeight.Medium,
                            color = if (isThisMonth) TealPrimary else Slate500
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .background(
                                color = if (!isThisMonth) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(11.dp)
                            )
                            .clickable { viewModel.setFilterScope("All") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All",
                            fontSize = 14.sp,
                            fontWeight = if (!isThisMonth) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isThisMonth) TealPrimary else Slate500
                        )
                    }
                }
            }

            // Summary row: Month / Scope label and Total Amount
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (filterScope == "This Month") selectedMonth.label else "All Time Expenses",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    Row {
                        Text(
                            text = "Total: ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Slate500
                        )
                        Text(
                            text = String.format(Locale.US, "%,.2f", totalAmount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "SAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500
                        )
                    }
                }
            }

            // Expenses List or Empty State
            if (filteredExpenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No expenses found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMain
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search or filters, or tap + to capture a receipt.",
                                fontSize = 13.sp,
                                color = Slate500,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Group by date or display directly
                items(filteredExpenses, key = { it.id }) { expense ->
                    val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(expense.dateMillis))
                    Column {
                        Text(
                            text = dateFormatted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate400,
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                        ExpenseListItem(
                            expense = expense,
                            onClick = { onNavigateToExpenseDetail(expense.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Expense
        FloatingActionButton(
            onClick = onNavigateToAddExpense,
            containerColor = TealPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("fab_add_expense")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Expense",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            FilterSheetContent(
                currentCategory = selectedCategory ?: "All",
                onApply = { cat ->
                    viewModel.setCategoryFilter(cat)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showFilterSheet = false
                    }
                },
                onReset = {
                    viewModel.resetFilters()
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showFilterSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun FilterSheetContent(
    currentCategory: String,
    onApply: (String) -> Unit,
    onReset: () -> Unit
) {
    var selectedCat by remember { mutableStateOf(currentCategory) }

    val categories = listOf("All", "Petrol", "Parking", "Other", "Maintenance", "Food")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .padding(bottom = 30.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )

            Text(
                text = "Reset",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TealPrimary,
                modifier = Modifier
                    .clickable { onReset() }
                    .testTag("reset_filter")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Category",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        categories.forEach { cat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedCat = cat }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (cat != "All") {
                        CategoryIconBadge(category = cat, size = 34)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = cat,
                        fontSize = 15.sp,
                        fontWeight = if (selectedCat.equals(cat, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                        color = TextMain
                    )
                }

                RadioButton(
                    selected = selectedCat.equals(cat, ignoreCase = true),
                    onClick = { selectedCat = cat },
                    colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onApply(selectedCat) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("apply_filter_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
        ) {
            Text(
                text = "Apply Filter",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
