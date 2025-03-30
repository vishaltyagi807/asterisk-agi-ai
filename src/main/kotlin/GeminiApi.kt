import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import okhttp3.OkHttpClient
import java.io.IOException


class GeminiService {

    private val messages = mutableListOf<Content>()

    suspend fun generateResponse(msg: String): String {
        return try {
            val prompt =
                "$msg\nProvide a plain text response without any UI elements such as buttons, tables, or code blocks. The response should be simple and easy to read aloud."
            val newRequest = Content(parts = listOf(Part(text = prompt)))
            val requestBody = GeminiRequest(contents = messages + newRequest)
            val response: HttpResponse = KtorClient.client.post(BASE_URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val result: GeminiResponse = response.body()
            val responseText = result.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text ?: "No response from Gemini AI"
            messages.add(Content(parts = listOf(Part(text = "User: $prompt"))))
            messages.add(Content(parts = listOf(Part(text = "AI: $responseText"))))

            responseText
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    companion object {
        private const val API_KEY = "AIzaSyCqxvDrd-yGd3iaYyDIH_5qrH8B5PuvSpo" // Replace with your actual API Key
        private const val BASE_URL =
            ("https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key="
                    + Companion.API_KEY)
    }
}


class GeminiApi internal constructor() {
    private val client = OkHttpClient()

    @Throws(IOException::class)
    fun getGeminiResponse(prompt: String?): String {
        var prompt = prompt
        prompt += "\nProvide a plain text response without any UI elements such as buttons, tables, or code blocks. The response should be simple and easy to read aloud."
        val userMessage = JsonObject()
        userMessage.addProperty("role", "user")
        userMessage.addProperty("text", prompt)
        conversationHistory.add(userMessage)

        val requestBody = JsonObject()
        val contentsArray = JsonArray()

        for (message in conversationHistory) {
            val contentObject = JsonObject()
            val partObject = JsonObject()
            partObject.addProperty("text", message["text"].asString)
            contentObject.add("parts", JsonArray())
            contentObject.getAsJsonArray("parts").add(partObject)
            contentsArray.add(contentObject)
        }

        requestBody.add("contents", contentsArray)
//
//        val body: RequestBody = RequestBody.create(
//            requestBody.toString(), get.get("application/json")
//        )


//        val request = Request.Builder()
//            .url(GEMINI_API_URL)
//            .post(body)
//            .build()

//        client.newCall(request).execute().use { response ->
//            if (!response.isSuccessful) {
//                throw IOException("Unexpected code $response")
//            }
//            val responseBody = response.body!!.string()
//            val botResponse = parseGeminiResponse(responseBody)
//
//            val aiMessage = JsonObject()
//            aiMessage.addProperty("role", "assistant")
//            aiMessage.addProperty("text", botResponse)
//            conversationHistory.add(aiMessage)
//            return botResponse
//        }
        return ""
    }

    companion object {
        private const val API_KEY = "AIzaSyCqxvDrd-yGd3iaYyDIH_5qrH8B5PuvSpo" // Replace with your actual API Key
        private const val GEMINI_API_URL =
            ("https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key="
                    + API_KEY)

        private val conversationHistory: MutableList<JsonObject> = ArrayList()

        private fun parseGeminiResponse(responseBody: String): String {
            val jsonResponse = JsonParser.parseString(responseBody).asJsonObject
            val candidates = jsonResponse.getAsJsonArray("candidates")

            if (candidates != null && candidates.size() > 0) {
                val firstCandidate = candidates[0].asJsonObject
                val parts = firstCandidate.getAsJsonObject("content").getAsJsonArray("parts")
                if (parts != null && parts.size() > 0) {
                    return parts[0].asJsonObject["text"].asString
                }
            }
            return "No response from Gemini AI."
        }
    }
}