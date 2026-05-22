package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini API Models ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Double? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// --- Productivity Prediction Response (Moshi Model) ---

@JsonClass(generateAdapter = true)
data class PriorityPredictionResponse(
    val prioritizationReasoning: String,
    val items: List<PredictedItem>,
    val productivityAdvice: List<String>
)

@JsonClass(generateAdapter = true)
data class PredictedItem(
    val id: Int,
    val priorityScore: Int,
    val reasoning: String
)

// --- Retrofit Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    val responseMoshi: Moshi = moshi
}

// --- API Client Implementation ---

object GeminiPredictEngine {
    private const val TAG = "GeminiPredictEngine"

    suspend fun predictPriorities(tasks: List<Task>, strategy: String = "BALANCED"): PriorityPredictionResponse? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or placeholder value!")
            return null
        }

        if (tasks.isEmpty()) {
            return PriorityPredictionResponse(
                prioritizationReasoning = "Your task list is empty. Add tasks to calculate your personalized focus predictions.",
                items = emptyList(),
                productivityAdvice = listOf(
                    "Define at least 2 key activities for today.",
                    "Tag tasks with Urgency & Importance to help the AI predictor identify focus points."
                )
            )
        }

        // Add strategy-specific prioritizing rules dynamically
        val strategyInstruction = when(strategy) {
            "EAT_THE_FROG" -> """
                Strategy focus guidelines: 'EAT THE FROG'. Analyze the workload and heavily prioritize complex, high-effort, or high-importance tasks first.
                The hardest or most demanding tasks must get the highest priority scores (85-100) so the user is encouraged to tackle them when mental focus is highest.
            """.trimIndent()
            "QUICK_WINS" -> """
                Strategy focus guidelines: 'QUICK WINS'. Prioritize short-duration, high-impact, or easier tasks first.
                Assign higher priority scores (85-100) to fast tasks that take less than 30 minutes, allowing the user to experience rapid accomplishment loops and clear the deck early.
            """.trimIndent()
            else -> """
                Strategy focus guidelines: 'BALANCED COMPASS'. Analyze and balance urgency, importance, and realistic focus density equally to create a sustainable, standard high-performance daily agenda.
            """.trimIndent()
        }

        // Format tasks in prompt
        val tasksText = tasks.joinToString("\n") { task ->
            "ID: ${task.id} | Title: \"${task.title}\" | Desc: \"${task.description}\" | Category: ${task.category} | Urgency: ${task.urgency} | Importance: ${task.importance} | Est: ${task.estimatedMinutes}m"
        }

        val prompt = """
            You are a Personal Productivity AI Coordinator. Analyze the user's workload below and predict their daily priorities.
            
            $strategyInstruction
            
            Assign custom priority Scores (1-100) where a higher score signifies a critical task that the user should complete FIRST.
            Provide reasoning for the priority of each task, focusing on urgency, importance, and realistic focus scheduling.

            Current tasks:
            $tasksText

            Respond STRICTLY adhering to this JSON layout format:
            {
              "prioritizationReasoning": "Your supportive, expert-level visual roadmap (2-3 sentences) detailing how the user should strategize their workload today.",
              "items": [
                {
                  "id": [Insert corresponding ID mapping here],
                  "priorityScore": [Int between 1 and 100],
                  "reasoning": "[1 clear reason detailing why this priority score and schedule was assigned, e.g. 'Work task of high importance and urgency is critical for a productive morning focus.']"
                }
              ],
              "productivityAdvice": [
                "Custom dynamic actionable instruction 1 based on task fatigue or load optimization",
                "Custom dynamic actionable instruction 2 based on task fatigue or load optimization"
              ]
            }

            Rule: Return only raw JSON. Do NOT wrap inside markdown block.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are a professional personal assistant designed to optimize workload scheduling and task prioritization by generating clean structured JSON."))
            )
        )

        return try {
            val response = RetrofitClient.apiService.generateContent(apiKey, request)
            val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (rawJson != null) {
                Log.d(TAG, "Raw Gemini JSON: $rawJson")
                val adapter = RetrofitClient.responseMoshi.adapter(PriorityPredictionResponse::class.java)
                adapter.fromJson(rawJson)
            } else {
                Log.e(TAG, "Empty response from Gemini")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini API", e)
            null
        }
    }
}
