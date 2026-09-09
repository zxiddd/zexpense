package com.example.data.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.coroutines.resume

data class OcrExpenseResult(
    val vendor: String,
    val category: String,
    val amount: Double,
    val vatAmount: Double,
    val dateMillis: Long,
    val notes: String,
    val rawText: String
)

object LocalReceiptOcrEngine {

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    suspend fun recognizeText(bitmap: Bitmap): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(Result.success(visionText.text))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    /**
     * Converts Eastern Arabic (Indic) numerals (٠-٩) and Arabic decimal comma (٫ / ,)
     * to standard Western Arabic numerals (0-9) and decimal points (.).
     */
    fun normalizeArabicDigits(input: String): String {
        val arabicChars = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val englishChars = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        var result = input
        for (i in arabicChars.indices) {
            result = result.replace(arabicChars[i], englishChars[i])
        }
        result = result.replace('٫', '.')
        return result
    }

    fun parseReceiptText(rawText: String): OcrExpenseResult {
        val normalizedText = normalizeArabicDigits(rawText)
        val lines = normalizedText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val lowerText = normalizedText.lowercase(Locale.ROOT)

        // 1. Detect Category based on multilingual keywords (Arabic & English)
        val category = when {
            lowerText.contains("fuel") || lowerText.contains("petrol") || lowerText.contains("gasoline") ||
                    lowerText.contains("diesel") || lowerText.contains("بنزين") || lowerText.contains("وقود") ||
                    lowerText.contains("ديزل") || lowerText.contains("محطة") || lowerText.contains("sasco") ||
                    lowerText.contains("aldrees") || lowerText.contains("petromin") || lowerText.contains("aramco") ||
                    lowerText.contains("naft") || lowerText.contains("sahel") || lowerText.contains("ساسكو") ||
                    lowerText.contains("الدريس") || lowerText.contains("بترومين") || lowerText.contains("أرامكو") ||
                    lowerText.contains("نفط") || lowerText.contains("سهل") -> "Petrol"

            lowerText.contains("parking") || lowerText.contains("mawgif") || lowerText.contains("mawgif") ||
                    lowerText.contains("مواقف") || lowerText.contains("موقف") || lowerText.contains("باركنج") ||
                    lowerText.contains("valet") || lowerText.contains("riyadh parking") -> "Parking"

            lowerText.contains("restaurant") || lowerText.contains("cafe") || lowerText.contains("coffee") ||
                    lowerText.contains("food") || lowerText.contains("burger") || lowerText.contains("مطعم") ||
                    lowerText.contains("مقهى") || lowerText.contains("كافيه") || lowerText.contains("وجبة") ||
                    lowerText.contains("al baik") || lowerText.contains("البيك") || lowerText.contains("starbucks") ||
                    lowerText.contains("mcdonald") || lowerText.contains("دانكن") || lowerText.contains("dunkin") ||
                    lowerText.contains("tim hortons") || lowerText.contains("barn") -> "Food"

            lowerText.contains("hotel") || lowerText.contains("resort") || lowerText.contains("stay") ||
                    lowerText.contains("فندق") || lowerText.contains("شقق فندقية") || lowerText.contains("أجنحة") ||
                    lowerText.contains("hospitality") -> "Accommodation"

            lowerText.contains("maintenance") || lowerText.contains("service") || lowerText.contains("oil change") ||
                    lowerText.contains("صيانة") || lowerText.contains("تغيير زيت") || lowerText.contains("غيار زيت") ||
                    lowerText.contains("بنشر") || lowerText.contains("إطارات") || lowerText.contains("ورشة") -> "Maintenance"

            lowerText.contains("hardware") || lowerText.contains("tools") || lowerText.contains("materials") ||
                    lowerText.contains("supplies") || lowerText.contains("معدات") || lowerText.contains("أدوات") ||
                    lowerText.contains("مواد") || lowerText.contains("مكتبة") || lowerText.contains("جرير") ||
                    lowerText.contains("jarir") || lowerText.contains("saco") || lowerText.contains("ساكو") -> "Supplies"

            else -> "Petrol" // Default for site/field engineer expenses
        }

        // 2. Identify Vendor Name (Arabic & English merchant dictionary + intelligent header cleanup)
        val merchantDictionary = listOf(
            // Fuel
            "Aramco" to listOf("aramco", "أرامكو", "ارامكو", "saudi aramco"),
            "SASCO" to listOf("sasco", "ساسكو", "شركة ساسكو"),
            "Aldrees" to listOf("aldrees", "الدريس", "شركة الدريس", "محطة الدريس"),
            "Petromin Express" to listOf("petromin", "بترومين", "بترومين اكسبرس", "بترومين إكسبرس"),
            "Naft Fuel" to listOf("naft", "شركة نفط", "محطة نفط"),
            "Sahel Station" to listOf("sahel", "محطات سهل", "شركة سهل"),
            "Shell Station" to listOf("shell", "شل"),
            "ADNOC Distribution" to listOf("adnoc", "أدنوك"),
            // Parking & Transit
            "Mawgif Parking" to listOf("mawgif", "موقف", "شركة موقف", "مواقف الوطنية"),
            "Riyadh Parking" to listOf("riyadh parking", "مواقف الرياض"),
            "Jeddah Parking" to listOf("jeddah parking", "مواقف جدة"),
            "Airport Parking" to listOf("airport parking", "مواقف المطار"),
            // Dining
            "Al Baik" to listOf("al baik", "albaik", "البيك"),
            "McDonald's" to listOf("mcdonald", "ماكدونالدز", "ماك"),
            "Starbucks Coffee" to listOf("starbucks", "ستاربكس"),
            "Tim Hortons" to listOf("tim hortons", "تيم هورتنز"),
            "Barn's Cafe" to listOf("barn", "بارنز", "بارن كافيه"),
            "Dunkin'" to listOf("dunkin", "دانكن"),
            "Half Million" to listOf("half million", "هاف مليون"),
            // Stores & Supplies
            "Jarir Bookstore" to listOf("jarir", "جرير", "مكتبة جرير"),
            "SACO Hardware" to listOf("saco", "ساكو"),
            "Extra Electronics" to listOf("extra", "اكسترا", "إكسترا"),
            "Panda Hypermarket" to listOf("panda", "بنده", "هايبر بنده"),
            "Danube Supermarket" to listOf("danube", "الدانوب", "أسواق الدانوب"),
            "Tamimi Markets" to listOf("tamimi", "أسواق التميمي", "التميمي"),
            "Al Othaim Markets" to listOf("othaim", "العثيم", "أسواق العثيم")
        )

        var detectedVendor: String? = null
        for ((canonicalName, aliases) in merchantDictionary) {
            if (aliases.any { alias -> lowerText.contains(alias.lowercase(Locale.ROOT)) }) {
                detectedVendor = canonicalName
                break
            }
        }

        // If not matched in dictionary, clean first non-header lines
        if (detectedVendor == null) {
            val ignoredHeaderKeywords = listOf(
                "simplified", "tax", "invoice", "receipt", "cash", "vat", "bill", "cr", "tel", "phone",
                "فاتورة", "ضريبية", "مبسطة", "إشعار", "سند", "قبض", "الرقم", "الضريبي", "سجل", "تجاري",
                "نرحب", "أهلا", "مرحبا", "welcome", "sales", "terminal", "station", "pos", "order"
            )

            for (line in lines.take(8)) {
                val lineLower = line.lowercase(Locale.ROOT)
                val isHeaderNoise = ignoredHeaderKeywords.any { kw -> lineLower.contains(kw) }
                val isNumberOnly = line.matches(Regex("^[0-9\\s\\-.:#]+$"))
                
                if (!isHeaderNoise && !isNumberOnly && line.length in 3..40) {
                    // Clean up special characters from the vendor title
                    val cleanLine = line.replace(Regex("[^a-zA-Z0-9\\s\\u0600-\\u06FF]"), "").trim()
                    if (cleanLine.length >= 3) {
                        detectedVendor = cleanLine
                        break
                    }
                }
            }
        }

        val vendor = detectedVendor ?: when (category) {
            "Petrol" -> "Fuel Station"
            "Parking" -> "Mawgif Parking"
            "Food" -> "Dining / Meal"
            "Maintenance" -> "Auto Maintenance"
            "Supplies" -> "Field Materials"
            else -> "Field Expense"
        }

        // 3. Detect Amount (Targeted extraction avoiding Tax IDs, CR, Dates, Phone, Times)
        val amountKeywords = listOf(
            // English total triggers
            "total amount", "total due", "grand total", "net total", "net amount", "total sar", "total sr",
            "invoice total", "amount due", "final total", "total (sar)", "amount (sar)", "total",
            // Arabic total triggers
            "المجموع الكلي", "المبلغ الإجمالي", "إجمالي الفاتورة", "المجموع", "الإجمالي شامل الضريبة",
            "الإجمالي", "الصافي", "المبلغ المستحق", "المدفوع", "المبلغ شامل الضريبة", "المبلغ"
        )

        var detectedAmount: Double? = null

        // Pass 1: Search for an amount directly associated with Total / Amount keywords
        for (i in lines.indices) {
            val line = lines[i]
            val lineLower = line.lowercase(Locale.ROOT)

            val matchedKeyword = amountKeywords.any { kw -> lineLower.contains(kw) }
            if (matchedKeyword) {
                // Check current line for a price e.g. "Total: 145.50" or "المجموع: ١٤٥٫٥٠"
                val priceInLine = extractPriceFromSnippet(line)
                if (priceInLine != null) {
                    detectedAmount = priceInLine
                    break
                }
                // If not on same line, check immediate next line (common in receipt layouts)
                if (i + 1 < lines.size) {
                    val nextPrice = extractPriceFromSnippet(lines[i + 1])
                    if (nextPrice != null) {
                        detectedAmount = nextPrice
                        break
                    }
                }
            }
        }

        // Pass 2: If no keyword hit, scan for clean currency formatted numbers in the text
        if (detectedAmount == null) {
            val candidateAmounts = mutableListOf<Double>()
            for (line in lines) {
                val candidate = extractPriceFromSnippet(line)
                if (candidate != null && candidate in 3.0..9999.0) {
                    candidateAmounts.add(candidate)
                }
            }
            if (candidateAmounts.isNotEmpty()) {
                // Receipt totals are typically the largest parsed currency value
                detectedAmount = candidateAmounts.maxOrNull()
            }
        }

        val amount = detectedAmount ?: when (category) {
            "Petrol" -> 120.00
            "Parking" -> 15.00
            "Food" -> 35.00
            else -> 50.00
        }

        // 4. Extract or Calculate VAT (15% Standard Saudi Rate)
        var detectedVat: Double? = null
        val vatKeywords = listOf("vat 15%", "vat", "ضريبة القيمة المضافة", "الضريبة", "15% vat", "tax 15%", "tax")
        for (line in lines) {
            val lineLower = line.lowercase(Locale.ROOT)
            if (vatKeywords.any { lineLower.contains(it) } && !lineLower.contains("total") && !lineLower.contains("إجمالي")) {
                val vatVal = extractPriceFromSnippet(line)
                if (vatVal != null && vatVal < amount) {
                    detectedVat = vatVal
                    break
                }
            }
        }
        val vatAmount = detectedVat ?: (amount * 0.15 / 1.15)

        // 5. Detect Date (Gregorian or Hijri formats)
        var detectedDateMillis = System.currentTimeMillis()
        val dateRegex = Pattern.compile("(\\d{1,4})[/-](\\d{1,2})[/-](\\d{1,4})")
        val dateMatcher = dateRegex.matcher(normalizedText)
        if (dateMatcher.find()) {
            val part1 = dateMatcher.group(1)?.toIntOrNull() ?: 0
            val part2 = dateMatcher.group(2)?.toIntOrNull() ?: 0
            val part3 = dateMatcher.group(3)?.toIntOrNull() ?: 0

            // Determine if YYYY-MM-DD or DD-MM-YYYY
            val (year, month, day) = if (part1 > 1900 && part1 < 2100) {
                Triple(part1, (part2 - 1).coerceIn(0, 11), part3.coerceIn(1, 31))
            } else if (part3 > 1900 && part3 < 2100) {
                Triple(part3, (part2 - 1).coerceIn(0, 11), part1.coerceIn(1, 31))
            } else if (part3 in 20..30) {
                Triple(2000 + part3, (part2 - 1).coerceIn(0, 11), part1.coerceIn(1, 31))
            } else {
                Triple(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
            }

            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
            }
            detectedDateMillis = cal.timeInMillis
        }

        val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(detectedDateMillis))
        val notes = "$vendor ($category) • $dateFormatted"

        return OcrExpenseResult(
            vendor = vendor,
            category = category,
            amount = amount,
            vatAmount = vatAmount,
            dateMillis = detectedDateMillis,
            notes = notes,
            rawText = rawText
        )
    }

    /**
     * Extracts a price decimal from a string snippet, actively rejecting Tax IDs (15 digits),
     * phone numbers (05xxxxxxxx), timestamps (12:30), invoice IDs (#1234), etc.
     */
    private fun extractPriceFromSnippet(text: String): Double? {
        // Strip timestamps like 14:20:30
        val textWithoutTime = text.replace(Regex("\\b\\d{1,2}:\\d{2}(:\\d{2})?\\b"), " ")

        // Strip 15-digit ZATCA VAT numbers (300xxxxxxxxxxxx) and 10-digit CR numbers
        val textWithoutIds = textWithoutTime.replace(Regex("\\b3\\d{14}\\b|\\b\\d{10,15}\\b"), " ")

        // Match decimal prices like 120.00, 95.50, 45, 1,250.75
        val priceRegex = Regex("(?<!\\d)([0-9]{1,4}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})|[0-9]+(?:\\.[0-9]{1,2}))(?!\\d)")
        val matches = priceRegex.findAll(textWithoutIds)

        for (match in matches) {
            val numStr = match.value.replace(",", "")
            val parsedVal = numStr.toDoubleOrNull()
            if (parsedVal != null && parsedVal in 1.0..99999.0) {
                // Ensure it wasn't a year like 2024, 2025, 2026 unless formatted with decimal e.g. 2024.00
                if ((parsedVal.toInt() in 2020..2035) && !numStr.contains(".")) {
                    continue
                }
                return parsedVal
            }
        }
        return null
    }
}
