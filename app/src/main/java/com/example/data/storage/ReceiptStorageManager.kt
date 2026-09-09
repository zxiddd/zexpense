package com.example.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReceiptStorageManager(private val context: Context) {

    private val receiptsBaseDir: File
        get() {
            val dir = File(context.filesDir, "Receipts")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun saveReceiptBitmap(bitmap: Bitmap, dateMillis: Long = System.currentTimeMillis()): String {
        val year = SimpleDateFormat("yyyy", Locale.US).format(Date(dateMillis))
        val month = SimpleDateFormat("MMMM", Locale.US).format(Date(dateMillis))

        val monthDir = File(receiptsBaseDir, "$year/$month")
        if (!monthDir.exists()) monthDir.mkdirs()

        val filename = "receipt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
        val file = File(monthDir, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        return file.absolutePath
    }

    fun saveReceiptFromUri(uri: Uri, dateMillis: Long = System.currentTimeMillis()): String? {
        return try {
            val year = SimpleDateFormat("yyyy", Locale.US).format(Date(dateMillis))
            val month = SimpleDateFormat("MMMM", Locale.US).format(Date(dateMillis))

            val monthDir = File(receiptsBaseDir, "$year/$month")
            if (!monthDir.exists()) monthDir.mkdirs()

            val filename = "receipt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val file = File(monthDir, filename)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getReceiptFile(path: String?): File? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        return if (file.exists()) file else null
    }

    fun deleteReceipt(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        val file = File(path)
        return if (file.exists()) file.delete() else false
    }

    /**
     * Creates a realistic rendered receipt graphic on-device for sample bills
     */
    fun createSampleReceipt(
        vendor: String,
        amount: Double,
        vat: Double,
        dateStr: String,
        category: String
    ): String {
        val width = 600
        val height = 900
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background - subtle textured paper color
        canvas.drawColor(Color.rgb(248, 246, 240))

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 24f
            typeface = Typeface.MONOSPACE
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 34f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(71, 85, 105)
            textSize = 20f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 26f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 2f
        }

        // Draw top zigzag / bill border
        var y = 60f
        canvas.drawText(vendor.uppercase(Locale.US), width / 2f, y, titlePaint)
        y += 35f
        canvas.drawText("TAX INVOICE / فاتورة ضريبية", width / 2f, y, centerPaint)
        y += 30f
        canvas.drawText("CR: 1010${(100000..999999).random()} - VAT: 3000${(1000000..9999999).random()}", width / 2f, y, centerPaint)
        y += 40f

        canvas.drawLine(40f, y, width - 40f, y, linePaint)
        y += 40f

        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Date / التاريخ: $dateStr", 40f, y, paint)
        y += 35f
        canvas.drawText("Category: $category", 40f, y, paint)
        y += 35f
        canvas.drawText("Receipt #: REC-${(10000..99999).random()}", 40f, y, paint)
        y += 40f

        canvas.drawLine(40f, y, width - 40f, y, linePaint)
        y += 45f

        // Table Header
        canvas.drawText("DESCRIPTION", 40f, y, boldPaint)
        boldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL (SAR)", width - 40f, y, boldPaint)
        y += 35f

        // Item row
        val subtotal = amount - vat
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(category, 40f, y, paint)
        val rightPaint = Paint(paint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText(String.format(Locale.US, "%.2f", subtotal), width - 40f, y, rightPaint)
        y += 50f

        canvas.drawLine(40f, y, width - 40f, y, linePaint)
        y += 40f

        canvas.drawText("Subtotal (SAR):", 40f, y, paint)
        canvas.drawText(String.format(Locale.US, "%.2f", subtotal), width - 40f, y, rightPaint)
        y += 35f

        canvas.drawText("VAT (15%):", 40f, y, paint)
        canvas.drawText(String.format(Locale.US, "%.2f", vat), width - 40f, y, rightPaint)
        y += 45f

        // Grand Total Box
        boldPaint.textAlign = Paint.Align.LEFT
        boldPaint.textSize = 32f
        canvas.drawText("TOTAL (SAR):", 40f, y, boldPaint)
        val bigTotalPaint = Paint(boldPaint).apply {
            textAlign = Paint.Align.RIGHT
            color = Color.rgb(16, 185, 129)
        }
        canvas.drawText(String.format(Locale.US, "%.2f", amount), width - 40f, y, bigTotalPaint)
        y += 50f

        canvas.drawLine(40f, y, width - 40f, y, linePaint)
        y += 50f

        // QR Code placeholder box
        val qrBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val qrFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }
        canvas.drawRect(width / 2f - 70f, y, width / 2f + 70f, y + 140f, qrFill)
        canvas.drawRect(width / 2f - 70f, y, width / 2f + 70f, y + 140f, qrBoxPaint)
        canvas.drawText("[ ZATCA QR CODE ]", width / 2f, y + 80f, centerPaint)

        y += 180f
        canvas.drawText("*** THANK YOU FOR YOUR VISIT ***", width / 2f, y, centerPaint)
        y += 30f
        canvas.drawText("ZExpense Local Storage Verification", width / 2f, y, centerPaint)

        return saveReceiptBitmap(bitmap)
    }
}
