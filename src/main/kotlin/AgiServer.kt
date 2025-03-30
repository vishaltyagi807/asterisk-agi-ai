import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.runBlocking
import org.asteriskjava.fastagi.AgiChannel
import org.asteriskjava.fastagi.AgiException
import org.asteriskjava.fastagi.AgiRequest
import org.asteriskjava.fastagi.AgiScript
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*



class AgiServer : AgiScript {

    private val stt: STT = STT()
    private val tts: TTS = TTS()
    private fun getLocalPath(completePath: String): String {
        return completePath.replace("/var/lib/asterisk/sounds/".toRegex(), "").replace(".wav", "")
    }


    override fun service(req: AgiRequest?, channel: AgiChannel?) {
        try {
            runBlocking {
                channel?.answer()
                val ai = GeminiService()
                val callerId: String = req!!.callerIdNumber
                channel?.streamFile("custom/welcome")
                val greeting = GREETINGS[Random().nextInt(GREETINGS.size)]
                channel!!.streamFile(greeting)
                while (true) {
                    channel.streamFile("custom/start_speaking")
                    val recordingFile = generateUniqueRecordingName(callerId)
                    channel.recordFile(
                        recordingFile,
                        RECORDING_FORMAT,
                        "#",
                        MAX_RECORDING_DURATION * 1000, 0, true, 3
                    )
                    println("Recording saved")
                    val prompt = stt.transcribeAudio(recordingFile)
                    val response = ai.generateResponse(prompt)
                    if (!response.startsWith("Error")) {
                        uploadUserData(callerId.toInt(), prompt, response)
                    }
                    val resOutFilePath = tts.getTextToSpeech(response, getResponseFile(callerId))
                    println(prompt)
                    println(response)
                    println(resOutFilePath)
                    val outPath = getLocalPath(resOutFilePath)
                    println(outPath)
                    channel.streamFile(outPath)
                    println("Speech Stoped.")
                }
            }
        } catch (e: AgiException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private suspend fun uploadUserData(user: Int, prompt: String, response: String) {
        SupaClient.client.from("chats").upsert(ChatMsg(UUID.randomUUID().toString(), user, prompt, response))
    }

    private fun generateUniqueRecordingName(callerId: String): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val recordingDir = File("$RECORDINGS_DIR$callerId/")
        if (!recordingDir.exists()) {
            recordingDir.mkdirs()
        }
        return File(recordingDir, "call_" + dateFormat.format(Date())).absolutePath
    }

    private fun getResponseFile(callerId: String): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val responseDir: File = File("$RESPONSES_DIR$callerId/")
        if (!responseDir.exists()) {
            responseDir.mkdirs()
        }
        return File(responseDir, "call_" + dateFormat.format(Date()) + ".wav").absolutePath
    }

    companion object {
        private const val RECORDINGS_DIR: String = "/var/lib/asterisk/sounds/recordings/"
        private const val RESPONSES_DIR: String = "/var/lib/asterisk/sounds/responses/"
        private const val MAX_RECORDING_DURATION: Int = 300
        private const val RECORDING_FORMAT: String = "wav"
        private val GREETINGS: Array<String> = arrayOf(
            "custom/greeting_01",
            "custom/greeting_02",
            "custom/greeting_03",
            "custom/greeting_04",
            "custom/greeting_05",
            "custom/greeting_06",
            "custom/greeting_07",
            "custom/greeting_08",
            "custom/greeting_09",
            "custom/greeting_10",
            "custom/greeting_11",
            "custom/greeting_12",
        )
    }
}