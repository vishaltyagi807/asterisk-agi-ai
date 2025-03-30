import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part> = emptyList(),
)

@Serializable
data class Part(
    val text: String? = null
)


@Serializable
data class GeminiResponse(val candidates: List<Candidate>?)

@Serializable
data class Candidate(val content: Content? = null)
