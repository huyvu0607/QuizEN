    package com.lumina.app.data.source.remote

    import retrofit2.http.GET
    import retrofit2.http.Path
    import retrofit2.Retrofit
    import retrofit2.converter.gson.GsonConverterFactory

    interface DictionaryApiService {
        @GET("entries/en/{word}")
        suspend fun getWordDetails(@Path("word") word: String): List<DictionaryEntry>

        companion object {
            private const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/"

            fun create(): DictionaryApiService {
                return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(DictionaryApiService::class.java)
            }
        }
    }

    data class DictionaryEntry(
        val word: String,
        val phonetic: String?,
        val phonetics: List<Phonetic>,
        val meanings: List<Meaning>
    )

    data class Phonetic(
        val text: String?,
        val audio: String?
    )

    data class Meaning(
        val partOfSpeech: String,
        val definitions: List<Definition>
    )

    data class Definition(
        val definition: String,
        val example: String?
    )
