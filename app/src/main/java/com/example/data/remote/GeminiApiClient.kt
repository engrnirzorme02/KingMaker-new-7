package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.ProvenanceMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class GeminiExecutionResult(
    val text: String,
    val audioBase64: String? = null,
    val provenanceMode: ProvenanceMode = ProvenanceMode.PROVIDER,
    val modelName: String,
    val thinkingText: String? = null,
    val groundingSources: List<String> = emptyList()
)

class GeminiApiClient {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY.ifBlank { "" }
        } catch (e: Throwable) {
            ""
        }

    fun hasValidApiKey(): Boolean {
        val key = apiKey
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * General Text / Reasoning Generation
     */
    suspend fun generateContent(
        model: String = "gemini-3.5-flash",
        prompt: String,
        systemInstruction: String? = null,
        enableHighThinking: Boolean = false,
        useGoogleSearch: Boolean = false,
        useGoogleMaps: Boolean = false,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): GeminiExecutionResult = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) {
            return@withContext getSimulatedResponse(model, prompt, systemInstruction)
        }

        try {
            val root = JSONObject()

            // System instruction
            if (!systemInstruction.isNullOrBlank()) {
                val sysInst = JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                }
                root.put("systemInstruction", sysInst)
            }

            // Tools (Search / Maps Grounding)
            val toolsArray = JSONArray()
            if (useGoogleSearch) {
                toolsArray.put(JSONObject().put("googleSearch", JSONObject()))
            }
            if (useGoogleMaps) {
                toolsArray.put(JSONObject().put("googleMaps", JSONObject()))
            }
            if (toolsArray.length() > 0) {
                root.put("tools", toolsArray)
            }

            // Contents (multi-turn or single-turn)
            val contentsArray = JSONArray()
            for ((role, text) in conversationHistory) {
                val msgObj = JSONObject().apply {
                    put("role", if (role == "user") "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", text)))
                }
                contentsArray.put(msgObj)
            }
            // Append current prompt
            val currentTurn = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            }
            contentsArray.put(currentTurn)
            root.put("contents", contentsArray)

            // Generation config (Thinking level if requested)
            val genConfig = JSONObject()
            if (enableHighThinking && model.contains("gemini-3.1-pro-preview")) {
                val thinkingConfig = JSONObject().apply {
                    put("thinkingLevel", "HIGH")
                }
                genConfig.put("thinkingConfig", thinkingConfig)
            }
            if (genConfig.length() > 0) {
                root.put("generationConfig", genConfig)
            }

            val requestBody = root.toString().toRequestBody(jsonMediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w("GeminiApiClient", "Request failed with code ${response.code}: $responseBody")
                return@withContext getSimulatedResponse(model, prompt, systemInstruction)
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val textSb = StringBuilder()
            var thinkingText: String? = null
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.optBoolean("thought", false)) {
                        thinkingText = part.optString("text")
                    } else if (part.has("text")) {
                        textSb.append(part.getString("text"))
                    }
                }
            }

            // Extract Grounding metadata if available
            val sources = mutableListOf<String>()
            val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val chunks = groundingMetadata.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        val uri = web?.optString("uri")
                        val title = web?.optString("title")
                        if (!uri.isNullOrBlank()) {
                            sources.add(if (!title.isNullOrBlank()) "$title ($uri)" else uri)
                        }
                    }
                }
            }

            GeminiExecutionResult(
                text = textSb.toString().ifBlank { "Analysis concluded." },
                provenanceMode = ProvenanceMode.PROVIDER,
                modelName = model,
                thinkingText = thinkingText,
                groundingSources = sources
            )
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Error calling Gemini API: ${e.message}", e)
            getSimulatedResponse(model, prompt, systemInstruction)
        }
    }

    /**
     * Multimodal Image Analysis (using gemini-3.1-pro-preview)
     */
    suspend fun analyzeImage(
        bitmap: Bitmap,
        prompt: String
    ): GeminiExecutionResult = withContext(Dispatchers.IO) {
        val model = "gemini-3.1-pro-preview"
        if (!hasValidApiKey()) {
            return@withContext getSimulatedResponse(model, prompt, "Image Understanding")
        }

        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val root = JSONObject().apply {
                val contents = JSONArray().put(
                    JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Data)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", parts)
                    }
                )
                put("contents", contents)
            }

            val requestBody = root.toString().toRequestBody(jsonMediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext getSimulatedResponse(model, prompt, "Image Architecture Extraction")
            }

            val json = JSONObject(responseBody)
            val text = json.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: "Architecture diagram extracted."

            GeminiExecutionResult(
                text = text,
                provenanceMode = ProvenanceMode.PROVIDER,
                modelName = model
            )
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Image analysis error: ${e.message}", e)
            getSimulatedResponse(model, prompt, "Image Architecture Extraction")
        }
    }

    /**
     * Text-to-Speech (TTS) using gemini-3.8-flash-tts
     */
    suspend fun generateSpeech(
        text: String
    ): GeminiExecutionResult = withContext(Dispatchers.IO) {
        val model = "gemini-3.8-flash-tts"
        if (!hasValidApiKey()) {
            return@withContext GeminiExecutionResult(
                text = "TTS playback simulated for decision summary.",
                audioBase64 = null,
                provenanceMode = ProvenanceMode.SIMULATED,
                modelName = model
            )
        }

        try {
            val root = JSONObject().apply {
                val contents = JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", "Read this decision summary clearly: $text")))
                    }
                )
                put("contents", contents)

                val genConfig = JSONObject().apply {
                    put("responseModalities", JSONArray().put("AUDIO"))
                    val speechConfig = JSONObject().apply {
                        val voiceConfig = JSONObject().apply {
                            val prebuiltVoiceConfig = JSONObject().apply {
                                put("voiceName", "Kore")
                            }
                            put("prebuiltVoiceConfig", prebuiltVoiceConfig)
                        }
                        put("voiceConfig", voiceConfig)
                    }
                    put("speechConfig", speechConfig)
                }
                put("generationConfig", genConfig)
            }

            val requestBody = root.toString().toRequestBody(jsonMediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext GeminiExecutionResult(
                    text = "TTS service unavailable.",
                    audioBase64 = null,
                    provenanceMode = ProvenanceMode.SIMULATED,
                    modelName = model
                )
            }

            val json = JSONObject(responseBody)
            val parts = json.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")

            var audioData: String? = null
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    val inline = part.optJSONObject("inlineData")
                    if (inline != null && inline.optString("mimeType", "").startsWith("audio/")) {
                        audioData = inline.optString("data")
                        break
                    }
                }
            }

            GeminiExecutionResult(
                text = "Audio synthesized successfully with voice 'Kore'.",
                audioBase64 = audioData,
                provenanceMode = ProvenanceMode.PROVIDER,
                modelName = model
            )
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "TTS generation error: ${e.message}", e)
            GeminiExecutionResult(
                text = "Audio generation fallback.",
                audioBase64 = null,
                provenanceMode = ProvenanceMode.SIMULATED,
                modelName = model
            )
        }
    }

    /**
     * Audio Transcription using gemini-3.5-transcribe
     */
    suspend fun transcribeAudio(
        audioBytes: ByteArray
    ): GeminiExecutionResult = withContext(Dispatchers.IO) {
        val model = "gemini-3.5-transcribe"
        if (!hasValidApiKey() || audioBytes.isEmpty()) {
            return@withContext GeminiExecutionResult(
                text = "মাইক্রোসার্ভিস আর্কিটেকচার থেকে মডুলার মনোলিথ-এ মাইগ্রেশন করা কি নিরাপদ হবে?",
                provenanceMode = ProvenanceMode.SIMULATED,
                modelName = model
            )
        }

        try {
            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
            val root = JSONObject().apply {
                val contents = JSONArray().put(
                    JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", "Please accurately transcribe this audio recording in the original spoken language (Bengali / English / Banglish)."))
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "audio/mp4")
                                    put("data", base64Audio)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", parts)
                    }
                )
                put("contents", contents)
            }

            val requestBody = root.toString().toRequestBody(jsonMediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext GeminiExecutionResult(
                    text = "মাইক্রোসার্ভিস নাকি মডুলার মনোলিথ — ডেটাবেস কনসিস্টেন্সি বজায় রাখার সেরা উপায় কি?",
                    provenanceMode = ProvenanceMode.SIMULATED,
                    modelName = model
                )
            }

            val json = JSONObject(responseBody)
            val text = json.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: "অডিও ট্রান্সক্রিপশন সম্পন্ন হয়েছে।"

            GeminiExecutionResult(
                text = text,
                provenanceMode = ProvenanceMode.PROVIDER,
                modelName = model
            )
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Transcription error: ${e.message}", e)
            GeminiExecutionResult(
                text = "উচ্চ-স্কেলে ক্যাশিং স্ট্র্যাটেজি হিসেবে Redis বনাম Memcached নির্বাচন।",
                provenanceMode = ProvenanceMode.SIMULATED,
                modelName = model
            )
        }
    }

    /**
     * Fallback deterministic simulated synthesis (Always explicitly labeled SIMULATED per I-14).
     */
    private fun getSimulatedResponse(model: String, prompt: String, systemInstruction: String?): GeminiExecutionResult {
        val simulatedText = when {
            prompt.contains("interpret", ignoreCase = true) || prompt.contains("ইন্টারপ্রেট", ignoreCase = true) -> {
                "সিদ্ধান্তের মূল প্রশ্ন: সিস্টেমে আর্কিটেকচারাল পরিবর্তন বাস্তবায়নে সবচেয়ে কার্যকর ও কম ঝুঁকিপূর্ণ পথ কোনটি?\n" +
                "- FACT: বিদ্যমান সিস্টেম একক ডেটাবেসে কাজ করছে।\n" +
                "- CONSTRAINT: ট্রানজ্যাকশনাল অ্যাটোমিসিটি কোনোভাবেই বিঘ্নিত করা যাবে না।\n" +
                "- UNKNOWN: সমসাময়িক কনকারেন্ট লোড পিক আওয়ারে ১০ গুণ বৃদ্ধি পেলে latency প্রভাব কত হবে?"
            }
            prompt.contains("critique", ignoreCase = true) || prompt.contains("devil", ignoreCase = true) -> {
                "[Devil's Advocate Critique - CDR-07]\n" +
                "প্রধান চ্যালেঞ্জ: ডিস্ট্রিবিউটেড স্টেট ব্যবহারের ক্ষেত্রে নেটওয়ার্ক পার্টিশন বা ফেইলিওর হলে ডেটাবেস অসঙ্গতি তৈরি হতে পারে।\n" +
                "প্রস্তাবিত টেস্ট: Chaos Engineering / Network Latency Injection টেস্ট পরিচালনা করুন।"
            }
            prompt.contains("perspective", ignoreCase = true) -> {
                "[Software Architect Perspective]\n" +
                "অবস্থান: মডুলার মনোলিথ আর্কিটেকচার নির্বাচন করা শ্রেয়। এটি ডোমেন বাউন্ডারি বজায় রাখবে এবং অপ্রয়োজনীয় ডিস্ট্রিবিউটেড ওভারহেড দূর করবে।"
            }
            else -> {
                "KingMaker বিশ্লেষণ সম্পন্ন হয়েছে। নীতি ও প্রমাণের ভিত্তিতে নির্বাচিত আর্কিটেকচার বিকল্পটি সুপারিশ করা হলো।"
            }
        }

        return GeminiExecutionResult(
            text = simulatedText,
            provenanceMode = ProvenanceMode.SIMULATED,
            modelName = model,
            thinkingText = "Deterministic simulation applied per KingMaker ADR-V7-016."
        )
    }
}
