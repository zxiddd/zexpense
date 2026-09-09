package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val currency: String = "SAR",
    val dateMillis: Long,
    val vendor: String,
    val category: String,
    val vatAmount: Double = 0.0,
    val notes: String = "",
    val localImagePath: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
