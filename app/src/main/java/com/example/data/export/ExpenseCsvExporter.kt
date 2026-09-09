package com.example.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.ExpenseEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseCsvExporter(private val context: Context) {

    fun exportToCsv(
        monthName: String,
        yearStr: String,
        expenses: List<ExpenseEntity>
    ): File? {
        return try {
            val outputDir = File(context.cacheDir, "exports")
            if (!outputDir.exists()) outputDir.mkdirs()

            val fileName = "Expenses_${monthName}_$yearStr.csv".replace(" ", "_")
            val file = File(outputDir, fileName)

            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val sb = StringBuilder()
            // Header
            sb.append("ID,Date,Vendor,Category,Amount (SAR),VAT (SAR),Sync Status,Notes,Has Local Receipt\n")

            for (exp in expenses) {
                val dateStr = dateFormatter.format(Date(exp.dateMillis))
                val vendorEscaped = "\"${exp.vendor.replace("\"", "\"\"")}\""
                val notesEscaped = "\"${exp.notes.replace("\"", "\"\"")}\""
                val hasReceipt = if (!exp.localImagePath.isNullOrBlank()) "YES" else "NO"
                sb.append("${exp.id},$dateStr,$vendorEscaped,${exp.category},${exp.amount},${exp.vatAmount},${exp.syncStatus.name},$notesEscaped,$hasReceipt\n")
            }

            FileOutputStream(file).use { out ->
                out.write(sb.toString().toByteArray())
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareCsv(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Expenses CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareTextSummary(
        monthName: String,
        yearStr: String,
        expenses: List<ExpenseEntity>
    ) {
        val total = expenses.sumOf { it.amount }
        val count = expenses.size
        val categories = expenses.groupBy { it.category }

        val sb = StringBuilder()
        sb.append("📊 *ZExpense Monthly Summary — $monthName $yearStr*\n\n")
        sb.append("💰 *Total Expenses:* ${String.format(Locale.US, "%.2f SAR", total)}\n")
        sb.append("🧾 *Total Bills:* $count\n\n")
        sb.append("*Category Breakdown:*\n")
        for ((cat, list) in categories) {
            val catTotal = list.sumOf { it.amount }
            val pct = if (total > 0) (catTotal / total) * 100 else 0.0
            sb.append("• $cat: ${String.format(Locale.US, "%.2f SAR", catTotal)} (${String.format(Locale.US, "%.0f%%", pct)})\n")
        }
        sb.append("\n🔒 _Scanned bills are stored 100% locally on device via ZExpense._")

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            putExtra(Intent.EXTRA_SUBJECT, "ZExpense Summary — $monthName $yearStr")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share Monthly Summary").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
