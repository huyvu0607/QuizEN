package com.lumina.app

import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.lumina.app.data.source.remote.ai.*

class GeminiTest {
    // Lưu ý: Thay API Key mới của bạn vào đây
    private val apiKey = ""

    @Test
    fun findAllWorkingModels() = runBlocking {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val service = retrofit.create(GeminiApiService::class.java)
        
        println("📡 Đang lấy danh sách tất cả model từ Google...")
        
        try {
            val modelListResponse = service.listModels(apiKey)
            val allModels = modelListResponse.models
            
            // Lọc các model hỗ trợ tạo nội dung (generateContent)
            val candidateModels = allModels.filter { 
                it.supportedGenerationMethods?.contains("generateContent") == true 
            }.map { it.name.removePrefix("models/") }

            if (candidateModels.isEmpty()) {
                println("⚠️ Không tìm thấy model nào hỗ trợ generateContent.")
                return@runBlocking
            }

            println("🔎 Tìm thấy ${candidateModels.size} model ứng viên. Bắt đầu kiểm tra Quota...\n")
            
            val request = GeminiRequest(listOf(Content(listOf(Part("Hi")))))
            val workingModels = mutableListOf<String>()
            
            for (model in candidateModels) {
                print("➡️ Thử model [$model]: ")
                try {
                    // Thử gọi API v1beta
                    val response = service.generateContentBeta(model, apiKey, request)
                    val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (text != null) {
                        println("✅ OK")
                        workingModels.add(model)
                    } else {
                        println("❓ Thành công nhưng không có text trả về.")
                    }
                } catch (e: Exception) {
                    val errorMsg = e.message ?: ""
                    when {
                        errorMsg.contains("429") -> println("❌ Hết Quota (429)")
                        errorMsg.contains("403") -> println("❌ Không có quyền/Key lỗi (403)")
                        errorMsg.contains("404") -> println("❌ Model không hỗ trợ v1beta (404)")
                        else -> println("❌ Lỗi: $errorMsg")
                    }
                }
            }
            
            if (workingModels.isNotEmpty()) {
                println("\n✨ CÁC MODEL BẠN CÓ THỂ SỬ DỤNG NGAY:")
                workingModels.forEach { println("   ⭐ $it") }
                println("\n💡 Khuyên dùng: 'gemini-1.5-flash-8b' hoặc 'gemini-2.0-flash-lite-001'")
            } else {
                println("\n🔥 Không có model nào khả dụng. Hãy kiểm tra lại API Key.")
            }

        } catch (e: Exception) {
            println("💥 Lỗi khi lấy danh sách model: ${e.message}")
            if (e.message?.contains("403") == true) {
                println("👉 Nguyên nhân: API Key của bạn bị chặn hoặc sai.")
            }
        }
    }
}
