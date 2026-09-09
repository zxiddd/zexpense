package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC, id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun observeExpenseById(id: Long): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getExpensesBetweenDates(startMillis: Long, endMillis: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE syncStatus = :status")
    suspend fun getExpensesBySyncStatus(status: SyncStatus): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("UPDATE expenses SET syncStatus = :status, lastSyncedAt = :timestamp WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus, timestamp: Long?)
}
