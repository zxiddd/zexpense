package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.ExpenseRepository
import com.example.data.storage.ReceiptStorageManager
import com.example.ui.ZExpenseApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ExpenseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val receiptStorageManager = ReceiptStorageManager(applicationContext)
        val repository = ExpenseRepository(database.expenseDao(), receiptStorageManager)

        val factory = ExpenseViewModelFactory(repository, receiptStorageManager)
        viewModel = ViewModelProvider(this, factory)[ExpenseViewModel::class.java]

        setContent {
            MyApplicationTheme {
                ZExpenseApp(viewModel = viewModel)
            }
        }
    }
}

