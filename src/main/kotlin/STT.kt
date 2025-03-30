import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.speech.v1.*
import com.google.protobuf.ByteString
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class STT internal constructor() {
    private lateinit var credentials: GoogleCredentials

    private lateinit var speechSettings: SpeechSettings
    private lateinit var speechClient: SpeechClient

    private var config: RecognitionConfig = RecognitionConfig.newBuilder()
        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
        .setLanguageCode("en-US")
        .build()

    init {
        try {
            credentials = GoogleCredentials
                .fromStream(FileInputStream("/root/asterisk-agi-ai/src/main/resources/credentials.json"))
            speechSettings = SpeechSettings.newBuilder().setCredentialsProvider { credentials }.build()
            speechClient = SpeechClient.create(speechSettings)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun transcribeAudio(filePath: String): String {
        val audioBytes = readFile("$filePath.wav")
        val audioData: ByteString = ByteString.copyFrom(audioBytes)
        val audio: RecognitionAudio = RecognitionAudio.newBuilder().setContent(audioData).build()
        val request: RecognizeRequest = RecognizeRequest.newBuilder()
            .setConfig(config)
            .setAudio(audio)
            .build()
        val response: RecognizeResponse = speechClient.recognize(request)
        val results: List<SpeechRecognitionResult> = response.getResultsList()
        var str = ""
        for (result in results) {
            if (str.isEmpty() || str.isBlank()) {
                str = result.getAlternatives(0).getTranscript()
            }
            println("Transcription: $str")
        }
        return str
    }

    @Throws(IOException::class)
    private fun readFile(path: String): ByteArray {
        val file = File(path)
        val fis = FileInputStream(file)
        val data = ByteArray(file.length().toInt())
        fis.read(data)
        fis.close()
        return data
    }

    fun close() {
        speechClient.close()
    }
}
