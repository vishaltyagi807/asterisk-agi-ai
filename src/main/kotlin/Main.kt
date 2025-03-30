import org.asteriskjava.fastagi.DefaultAgiServer

fun main() {
    val agi = AgiServer()
    val server = DefaultAgiServer(agi)
    try {
        server.startup()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

