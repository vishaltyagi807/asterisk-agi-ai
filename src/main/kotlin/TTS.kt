import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.texttospeech.v1.*
import com.google.protobuf.ByteString
import kotlinx.io.IOException
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

class TTS internal constructor() {
    private lateinit var credentials: GoogleCredentials
    private lateinit var settings: TextToSpeechSettings
    private lateinit var textToSpeechClient: TextToSpeechClient
    private lateinit var voice: VoiceSelectionParams
    private lateinit var audioConfig: AudioConfig

    init {
        try {
            credentials = GoogleCredentials
                .fromStream(FileInputStream("/root/asterisk-agi-ai/src/main/resources/credentials.json"))
            settings = TextToSpeechSettings.newBuilder().setCredentialsProvider({ credentials }).build()
            textToSpeechClient = TextToSpeechClient.create(settings)
            voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode("en-US")
                .setSsmlGender(SsmlVoiceGender.FEMALE)
                .build()
            audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.LINEAR16)
                .setSampleRateHertz(8000)
                .build()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun getTextToSpeech(text: String?, outputFilePath: String): String {
        val input: SynthesisInput = SynthesisInput.newBuilder().setText(text).build()
        val response: SynthesizeSpeechResponse = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig)
        val audioContents: ByteString = response.getAudioContent()
        FileOutputStream(outputFilePath).use { out ->
            out.write(audioContents.toByteArray())
        }
        return outputFilePath
    }

    fun close() {
        textToSpeechClient.close()
    }
}