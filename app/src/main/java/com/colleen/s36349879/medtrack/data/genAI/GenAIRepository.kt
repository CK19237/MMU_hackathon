package com.colleen.s36349879.medtrack.data.genAI

import android.content.Context
import com.colleen.s36349879.medtrack.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

/**
 * Repository responsible for communicating with the Gemini AI API.
 *
 * This class initializes the [GenerativeModel] with the appropriate
 * model name and API key, and exposes a suspend function to fetch
 * AI-generated responses based on a given prompt.
 *
 * @param context The application context.
 */
class GenAIRepository (context: Context){

    /**
     * The Gemini generative model instance used to send prompts
     * and receive AI-generated content.
     *
     * Configured with the "gemini-2.5-flash" model and the API key
     * stored securely in [BuildConfig].
     */
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    /**
     * Sends a prompt to the Gemini AI model and returns the generated response.
     *
     * @param prompt The input text to send to the AI model.
     * @return The AI-generated response text, or null if an error occurred.
     */
    suspend fun getAiResponse(prompt: String): String? {
        return try {
            val response = generativeModel.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            null // Return null so the ViewModel can handle the error message
        }
    }

}