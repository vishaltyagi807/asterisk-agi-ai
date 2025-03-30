import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import okhttp3.OkHttpClient
import java.io.IOException
import org.apache.http.HttpException


class GeminiService {

    private val messages = mutableListOf<Content>()

    suspend fun generateResponse(msg: String): String {
        return try {
            val prompt =
                "$msg\nProvide a plain text response without any UI elements such as buttons, tables, or code blocks. The response should be simple and easy to read aloud."
            val newRequest = Content(parts = listOf(Part(text = prompt)))
            val requestBody = GeminiRequest(contents = listOf(newRequest))
            //val requestBody = GeminiRequest(contents = messages + newRequest)
            val response: HttpResponse = KtorClient.client.post(BASE_URL) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            println("Req : $requestBody")
            println("Status : ${response.status}")
            println("Raw Response : $response")
            val result: GeminiResponse = response.body()
            val responseText = result.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text ?: "No response from Gemini AI"
            messages.add(Content(parts = listOf(Part(text = "Previous Chat data of User request: $prompt"))))
            messages.add(Content(parts = listOf(Part(text = "Previous Chat data of AI response: $responseText"))))
            responseText
        }catch (e : HttpException){
            "Error: ${e.message}"
        }  catch (e: Exception) {
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
