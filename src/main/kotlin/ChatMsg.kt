import kotlinx.serialization.Serializable

@Serializable
data class ChatMsg(
    val id: String,
    val user: Int,
    val prompt: String,
    val response: String,
)
