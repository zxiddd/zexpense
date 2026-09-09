package com.example.data.repository

import com.example.data.local.ExpenseDao
import com.example.data.local.ExpenseEntity
import com.example.data.storage.ReceiptStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val receiptStorageManager: ReceiptStorageManager
) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    suspend fun getExpenseById(id: Long): ExpenseEntity? {
        return withContext(Dispatchers.IO) {
            expenseDao.getExpenseById(id)
        }
    }

    fun observeExpenseById(id: Long): Flow<ExpenseEntity?> {
        return expenseDao.observeExpenseById(id)
    }

    suspend fun addExpense(
        amount: Double,
        currency: String = "SAR",
        dateMillis: Long,
        vendor: String,
        category: String,
        vatAmount: Double,
        notes: String,
        localImagePath: String?
    ): Long {
        return withContext(Dispatchers.IO) {
            val expense = ExpenseEntity(
                amount = amount,
                currency = currency,
                dateMillis = dateMillis,
                vendor = vendor,
                category = category,
                vatAmount = vatAmount,
                notes = notes,
                localImagePath = localImagePath
            )
            expenseDao.insertExpense(expense)
        }
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        withContext(Dispatchers.IO) {
            expenseDao.updateExpense(expense)
        }
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        withContext(Dispatchers.IO) {
            expense.localImagePath?.let {
                receiptStorageManager.deleteReceipt(it)
            }
            expenseDao.deleteExpense(expense)
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        withContext(Dispatchers.IO) {
            val currentList = expenseDao.getAllExpenses().first()
            if (currentList.isNotEmpty()) return@withContext

            val cal = Calendar.getInstance()
            // September 2026 base
            cal.set(2026, Calendar.SEPTEMBER, 1, 10, 30)

            // 1. Aramco Petrol 120.00
            val img1 = receiptStorageManager.createSampleReceipt("Aramco Station", 120.00, 15.65, "01 Sep 2026", "Petrol")
            expenseDao.insertExpense(
                ExpenseEntity(
                    amount = 120.00,
                    currency = "SAR",
                    dateMillis = cal.timeInMillis,
                    vendor = "Aramco",
                    category = "Petrol",
                    vatAmount = 15.65,
                    notes = "Refueling for site visit trip #1",
                    localImagePath = img1
                )
            )

            // 2. Petromin Petrol 95.00
            cal.set(2026, Calendar.SEPTEMBER, 4, 14, 15)
            val img2 = receiptStorageManager.createSampleReceipt("Petromin Express", 95.00, 12.39, "04 Sep 2026", "Petrol")
            expenseDao.insertExpense(
                ExpenseEntity(
                    amount = 95.00,
                    currency = "SAR",
                    dateMillis = cal.timeInMillis,
                    vendor = "Petromin",
                    category = "Petrol",
                    vatAmount = 12.39,
                    notes = "Highway patrol vehicle refill",
                    localImagePath = img2
                )
            )

            // 3. SASCO Petrol 87.00
            cal.set(2026, Calendar.SEPTEMBER, 8, 9, 45)
            val img3 = receiptStorageManager.createSampleReceipt("SASCO Fuel", 87.00, 11.35, "08 Sep 2026", "Petrol")
            expenseDao.insertExpense(
                ExpenseEntity(
                    amount = 87.00,
                    currency = "SAR",
                    dateMillis = cal.timeInMillis,
                    vendor = "SASCO",
                    category = "Petrol",
                    vatAmount = 11.35,
                    notes = "Dammam Highway rest stop",
                    localImagePath = img3
                )
            )

            // 4. Parking Jeddah 15.00
            cal.set(2026, Calendar.SEPTEMBER, 12, 16, 20)
            val img4 = receiptStorageManager.createSampleReceipt("Mawgif Parking", 15.00, 1.96, "12 Sep 2026", "Parking")
            expenseDao.insertExpense(
                ExpenseEntity(
                    amount = 15.00,
                    currency = "SAR",
                    dateMillis = cal.timeInMillis,
                    vendor = "Jeddah",
                    category = "Parking",
                    vatAmount = 1.96,
                    notes = "Municipal site permit zone",
                    localImagePath = img4
                )
            )

            // 5. Petrol Aramco 87.00 (same day)
            cal.set(2026, Calendar.SEPTEMBER, 12, 18, 0)
            val img5 = receiptStorageManager.createSampleReceipt("Aramco Corniche", 87.00, 11.35, "12 Sep 2026", "Petrol")
            expenseDao.insertExpense(
                ExpenseEntity(
                    amount = 87.00,
                    currency = "SAR",
                    dateMillis = cal.timeInMillis,
                    vendor = "Aramco",
                    category = "Petrol",
                    vatAmount = 11.35,
                    notes = "Return journey",
                    localImagePath = img5
                )
            )

            // 6. Site Supplies 45.00
            cal.set(2026, Calendar.SEPTEMBER, 15, 11, 10)
            val img6 = receiptStorageManager.createSampleReceipt("Site Supplies Co", 45.00, 5.87, "15 Sep 2026", "Other")
            expenseDao.insertExpense(
                ExpenseEntity(
                    amount = 45.00,
                    currency = "SAR",
                    dateMillis = cal.timeInMillis,
                    vendor = "Site Supplies",
                    category = "Other",
                    vatAmount = 5.87,
                    notes = "Cable ties and safety tape",
                    localImagePath = img6
                )
            )

            val mockVendors = listOf(
                Triple("Aramco", "Petrol", 210.0),
                Triple("SASCO", "Petrol", 195.0),
                Triple("Petromin Express", "Petrol", 350.0),
                Triple("Shell Select", "Petrol", 180.0),
                Triple("ADNOC Oasis", "Petrol", 220.0),
                Triple("Airport Parking", "Parking", 50.0),
                Triple("Mall Parking", "Parking", 35.0),
                Triple("Riyadh Downtown Parking", "Parking", 40.0),
                Triple("Jeddah Seaport Parking", "Parking", 60.0),
                Triple("Field Tool Depot", "Other", 120.0),
                Triple("Safety Equipment", "Other", 135.0),
                Triple("Aramco Highway", "Petrol", 450.0),
                Triple("SASCO Express", "Petrol", 390.0),
                Triple("Petromin Quick Lube", "Petrol", 480.0),
                Triple("Aramco Depot", "Petrol", 520.0),
                Triple("SASCO Rest", "Petrol", 610.0),
                Triple("Terminal Parking", "Parking", 50.0)
            )

            for ((i, item) in mockVendors.withIndex()) {
                val day = (i % 25) + 3
                cal.set(2026, Calendar.SEPTEMBER, day, 10 + (i % 8), 10 + (i * 3) % 50)
                val img = receiptStorageManager.createSampleReceipt(item.first, item.third, item.third * 0.15 / 1.15, "$day Sep 2026", item.second)
                expenseDao.insertExpense(
                    ExpenseEntity(
                        amount = item.third,
                        currency = "SAR",
                        dateMillis = cal.timeInMillis,
                        vendor = item.first,
                        category = item.second,
                        vatAmount = item.third * 0.15 / 1.15,
                        notes = "Field expense #$i",
                        localImagePath = img
                    )
                )
            }
        }
    }
}
