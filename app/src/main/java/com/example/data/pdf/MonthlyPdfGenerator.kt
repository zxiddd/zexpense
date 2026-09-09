package com.example.data.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.ExpenseEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MonthlyPdfGenerator(private val context: Context) {

    fun generateMonthlyPdf(
        employeeName: String,
        monthName: String,
        yearStr: String,
        expenses: List<ExpenseEntity>
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // Standard A4 (595 x 842 pt at 72dpi)
            val pageHeight = 842
            var pageNumber = 1

            // Colors
            val primaryColor = Color.rgb(37, 99, 235) // Blue #2563EB
            val darkColor = Color.rgb(15, 23, 42)
            val grayColor = Color.rgb(100, 116, 139)
            val lightBg = Color.rgb(241, 245, 249)
            val borderGray = Color.rgb(226, 232, 240)

            // Paints
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryColor
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = darkColor
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = darkColor
                textSize = 10f
                typeface = Typeface.DEFAULT
            }
            val grayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = grayColor
                textSize = 9f
                typeface = Typeface.DEFAULT
            }
            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryColor
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderGray
                strokeWidth = 1f
            }
            val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = lightBg
                style = Paint.Style.FILL
            }

            // PAGE 1: Summary & Table
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = 50f
            // Brand Logo & Header
            canvas.drawText("ZEXPENSE", 40f, y, titlePaint)
            grayTextPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("CONFIDENTIAL EXPENSE CLAIM", pageWidth - 40f, y, grayTextPaint)
            grayTextPaint.textAlign = Paint.Align.LEFT
            y += 20f

            canvas.drawText("MONTHLY EXPENSE CLAIM REPORT", 40f, y, subtitlePaint)
            y += 25f

            // Info Box
            val infoBox = RectF(40f, y, pageWidth - 40f, y + 60f)
            canvas.drawRoundRect(infoBox, 8f, 8f, cardBgPaint)

            canvas.drawText("Employee: $employeeName", 55f, y + 24f, subtitlePaint)
            canvas.drawText("Claim Period: $monthName $yearStr", 55f, y + 44f, bodyPaint)

            val totalAmount = expenses.sumOf { it.amount }
            val totalVat = expenses.sumOf { it.vatAmount }

            grayTextPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Generated: ${SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())}", pageWidth - 55f, y + 24f, grayTextPaint)
            canvas.drawText("Currency: SAR", pageWidth - 55f, y + 44f, grayTextPaint)
            grayTextPaint.textAlign = Paint.Align.LEFT

            y += 80f

            // Table Headers
            val colDate = 50f
            val colCat = 140f
            val colVendor = 250f
            val colVat = 380f
            val colAmt = pageWidth - 50f

            canvas.drawText("DATE", colDate, y, headerPaint)
            canvas.drawText("CATEGORY", colCat, y, headerPaint)
            canvas.drawText("VENDOR / STATION", colVendor, y, headerPaint)
            val rightHeader = Paint(headerPaint).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("VAT (SAR)", colVat, y, rightHeader)
            canvas.drawText("AMOUNT (SAR)", colAmt, y, rightHeader)
            y += 8f

            canvas.drawLine(40f, y, pageWidth - 40f, y, linePaint)
            y += 18f

            val rightBody = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }
            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)

            // Table Rows
            val maxRowsPage1 = 18
            val page1Expenses = expenses.take(maxRowsPage1)

            for (exp in page1Expenses) {
                val dStr = dateFormatter.format(Date(exp.dateMillis))
                canvas.drawText(dStr, colDate, y, bodyPaint)
                canvas.drawText(exp.category, colCat, y, bodyPaint)
                canvas.drawText(exp.vendor.take(22), colVendor, y, bodyPaint)
                canvas.drawText(String.format(Locale.US, "%.2f", exp.vatAmount), colVat, y, rightBody)
                canvas.drawText(String.format(Locale.US, "%.2f", exp.amount), colAmt, y, rightBody)
                y += 18f
                canvas.drawLine(40f, y - 6f, pageWidth - 40f, y - 6f, linePaint)
            }

            // Summary Totals Card
            y = maxOf(y + 10f, 620f)
            val totalBox = RectF(40f, y, pageWidth - 40f, y + 80f)
            val totalBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(238, 242, 255)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(totalBox, 8f, 8f, totalBoxPaint)

            canvas.drawText("TOTAL CLAIM:", 60f, y + 32f, subtitlePaint)
            canvas.drawText("Number of Attached Bills: ${expenses.size}", 60f, y + 55f, bodyPaint)

            val bigTotalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryColor
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(String.format(Locale.US, "%.2f SAR", totalAmount), pageWidth - 60f, y + 34f, bigTotalPaint)
            canvas.drawText(String.format(Locale.US, "Incl. VAT: %.2f SAR", totalVat), pageWidth - 60f, y + 55f, rightBody)

            // Signatures
            y += 110f
            canvas.drawLine(60f, y, 220f, y, linePaint)
            canvas.drawText("Employee Signature", 60f, y + 15f, grayTextPaint)

            canvas.drawLine(pageWidth - 220f, y, pageWidth - 60f, y, linePaint)
            canvas.drawText("Manager / Finance Approval", pageWidth - 220f, y + 15f, grayTextPaint)

            // Footer
            canvas.drawText("Generated securely via ZExpense (Local-First Documents)", 40f, pageHeight - 30f, grayTextPaint)
            grayTextPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Page $pageNumber", pageWidth - 40f, pageHeight - 30f, grayTextPaint)
            grayTextPaint.textAlign = Paint.Align.LEFT

            pdfDocument.finishPage(page)

            // PAGE 2+: Attached Bills Gallery & Evidence
            val expensesWithImages = expenses.filter { !it.localImagePath.isNullOrBlank() }
            if (expensesWithImages.isNotEmpty()) {
                val chunks = expensesWithImages.chunked(2) // 2 receipts per page
                for (chunk in chunks) {
                    pageNumber++
                    val billPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    val billPage = pdfDocument.startPage(billPageInfo)
                    val billCanvas = billPage.canvas

                    var by = 50f
                    billCanvas.drawText("ATTACHED BILLS & RECEIPTS", 40f, by, subtitlePaint)
                    grayTextPaint.textAlign = Paint.Align.RIGHT
                    billCanvas.drawText("Claim Period: $monthName $yearStr", pageWidth - 40f, by, grayTextPaint)
                    grayTextPaint.textAlign = Paint.Align.LEFT
                    by += 20f
                    billCanvas.drawLine(40f, by, pageWidth - 40f, by, linePaint)
                    by += 20f

                    for (exp in chunk) {
                        // Card Header
                        val expCard = RectF(40f, by, pageWidth - 40f, by + 340f)
                        billCanvas.drawRoundRect(expCard, 8f, 8f, cardBgPaint)

                        val dStr = dateFormatter.format(Date(exp.dateMillis))
                        billCanvas.drawText("${exp.vendor} — ${exp.category}", 55f, by + 25f, subtitlePaint)
                        val amtStr = String.format(Locale.US, "%.2f SAR", exp.amount)
                        rightBody.color = primaryColor
                        rightBody.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        billCanvas.drawText(amtStr, pageWidth - 55f, by + 25f, rightBody)
                        rightBody.color = darkColor
                        rightBody.typeface = Typeface.DEFAULT

                        billCanvas.drawText("Date: $dStr | VAT: ${String.format(Locale.US, "%.2f SAR", exp.vatAmount)} | Notes: ${exp.notes.ifBlank { "N/A" }}", 55f, by + 42f, grayTextPaint)

                        // Render image
                        exp.localImagePath?.let { path ->
                            val imgFile = File(path)
                            if (imgFile.exists()) {
                                try {
                                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                                    if (bitmap != null) {
                                        val targetW = 220f
                                        val scale = targetW / bitmap.width
                                        val targetH = (bitmap.height * scale).coerceAtMost(260f)
                                        val dstRect = RectF(
                                            pageWidth / 2f - targetW / 2f,
                                            by + 55f,
                                            pageWidth / 2f + targetW / 2f,
                                            by + 55f + targetH
                                        )
                                        billCanvas.drawBitmap(bitmap, null, dstRect, null)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        by += 360f
                    }

                    // Footer
                    billCanvas.drawText("ZExpense Privacy: Stored locally on user device", 40f, pageHeight - 30f, grayTextPaint)
                    grayTextPaint.textAlign = Paint.Align.RIGHT
                    billCanvas.drawText("Page $pageNumber", pageWidth - 40f, pageHeight - 30f, grayTextPaint)
                    grayTextPaint.textAlign = Paint.Align.LEFT

                    pdfDocument.finishPage(billPage)
                }
            }

            // Save PDF to cache/documents
            val outputDir = File(context.cacheDir, "claims")
            if (!outputDir.exists()) outputDir.mkdirs()
            val fileName = "Monthly_Expense_Claim_${monthName}_$yearStr.pdf".replace(" ", "_")
            val pdfFile = File(outputDir, fileName)

            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun sharePdf(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share Expense Claim PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun viewPdf(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            sharePdf(file)
        }
    }
}
