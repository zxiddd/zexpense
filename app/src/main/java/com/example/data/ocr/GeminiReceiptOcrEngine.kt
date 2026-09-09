package com.example.data.ocr

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object GeminiReceiptOcrEngine {

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun Bitmap.toBase64(maxDimension: Int = 1200): String {
        val scaled = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val (w, h) = if (ratio > 1) {
                maxDimension to (maxDimension / ratio).toInt()
            } else {
                (maxDimension * ratio).toInt() to maxDimension
            }
            Bitmap.createScaledBitmap(this, w, h, true)
        } else {
            this
        }
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun analyzeReceipt(bitmap: Bitmap): Result<OcrExpenseResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(Exception("Gemini API key not configured"))
            }

            val base64Image = bitmap.toBase64()

            val prompt = """
                You are an expert OCR parser for expense receipts in Saudi Arabia (SAR) and global receipts.
                The receipt may be in Arabic, English, or bilingual.
                Analyze this receipt image and extract the following JSON strictly:
                {
                  "vendor": "Merchant/Station name (e.g. SASCO / ساسكو, Aldrees / الدريس, Aramco / أرامكو, Petromin, Mawgif, Al Baik, etc.)",
                  "category": "Petrol" | "Parking" | "Food" | "Accommodation" | "Supplies" | "Maintenance" | "Other",
                  "amount": 123.45,
                  "vatAmount": 16.10,
                  "currency": "SAR",
                  "date": "YYYY-MM-DD",
                  "notes": "Brief summary of purchased items or fuel/parking details"
                }

                CRITICAL RULES:
                1. Do NOT confuse 15-digit ZATCA VAT numbers (e.g., 300xxxxxxxxxxxx), 10-digit CR numbers, phone numbers (05xxxxxxxx), invoice numbers, or timestamps (HH:mm) with the total price or vendor name.
                2. Extract the actual Total / Payable Amount. Convert any Arabic-Indic numerals (٠-٩) to standard decimal numbers.
                3. If VAT is not printed explicitly, calculate 15% VAT: amount * 0.15 / 1.15.
                4. Return ONLY raw JSON without markdown code blocks, backticks, or extra commentary.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("response_mime_type", "application/json")
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini API error: ${response.code} ${response.message}"))
            }

            val responseBody = response.body?.string().orEmpty()
            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text").orEmpty()

            if (rawText.isBlank()) {
                return@withContext Result.failure(Exception("Empty response from AI parser"))
            }

            // Clean json text if wrapped in ```json
            val cleanJson = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsed = JSONObject(cleanJson)
            val vendor = parsed.optString("vendor").ifBlank { "Field Merchant" }
            val category = parsed.optString("category").ifBlank { "Petrol" }
            val amount = parsed.optDouble("amount", 0.0)
            val vatAmount = parsed.optDouble("vatAmount", if (amount > 0) amount * 0.15 / 1.15 else 0.0)
            val dateStr = parsed.optString("date")
            val notes = parsed.optString("notes").ifBlank { "$vendor ($category)" }

            var dateMillis = System.currentTimeMillis()
            if (dateStr.isNotBlank()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    sdf.parse(dateStr)?.let { dateMillis = it.time }
                } catch (e: Exception) {
                    // Ignore date parse fallback
                }
            }

            val result = OcrExpenseResult(
                vendor = vendor,
                category = category,
                amount = if (amount > 0) amount else 0.0,
                vatAmount = if (vatAmount > 0) vatAmount else (amount * 0.15 / 1.15),
                dateMillis = dateMillis,
                notes = notes,
                rawText = rawText
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
