import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupaClient {

    val client = createSupabaseClient(
        "https://ynjndisihnwxrlboykdl.supabase.co",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inluam5kaXNpaG53eHJsYm95a2RsIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0MzI4MjU2OSwiZXhwIjoyMDU4ODU4NTY5fQ.-7iA6jR1tyQfqxi31LVuaLQCMi5Vi1fGwS2WweDmebI"
    ) {
        install(Postgrest)
    }

}