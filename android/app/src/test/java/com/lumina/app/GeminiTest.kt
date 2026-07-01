package com.lumina.app

import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.lumina.app.data.source.remote.ai.*

class GeminiTest {
    private val apiKey = "AIzaSyDFwEGCDTt-Qm_77aQ6qkK-QGEzZIKE8yQ"

    @Test
    fun findWorkingModel() = runBlocking {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val service = retrofit.create(GeminiApiService::class.java)
        val request = GeminiRequest(listOf(Content(listOf(Part("Hi")))))
        
        // Danh sách các model tiềm năng từ kết quả listModels của bạn
        val candidateModels = listOf(
            "gemini-flash-latest",
            "gemini-2.0-flash-lite",
            "gemini-pro-latest",
            "gemini-1.5-flash", // Thử lại bản này qua Direct API
            "gemini-2.5-flash-lite"
        )
        
        println("🚀 Đang dò tìm model có hạn mức (Quota) khả dụng...")
        
        for (model in candidateModels) {
            print("🔎 Thử model $model: ")
            try {
                val response = service.generateContentBeta(model, apiKey, request)
                val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null) {
                    println("✅ THÀNH CÔNG! Hãy dùng model này.")
                    return@runBlocking
                }
            } catch (e: Exception) {
                if (e.message?.contains("429") == true) {
                    println("❌ Hết hạn mức (429).")
                } else if (e.message?.contains("404") == true) {
                    println("❌ Không tìm thấy (404).")
                } else {
                    println("❌ Lỗi: ${e.message}")
                }
            }
        }
        println("🔥 Tất cả model thử nghiệm đều không khả dụng. Bạn nên tạo API Key mới ở một Google Project khác.")
    }
}
