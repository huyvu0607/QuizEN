package com.lumina.app.data.source.remote.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(apiKey: String) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.2f
            topK = 32
            topP = 1f
            maxOutputTokens = 2048
        }
    )

    suspend fun generateContent(prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun explainVocabulary(word: String, meaning: String): String? {
        val prompt = """
            Explain the English word '$word' which means '$meaning' in Vietnamese. 
            Provide:
            1. A simple definition in English.
            2. Two common example sentences.
            3. A short note on common usage or synonyms.
            Keep the response concise and friendly for a language learner.
        """.trimIndent()
        return generateContent(prompt)
    }

    suspend fun suggestFullVocabulary(word: String): String? {
        val prompt = """
            Bạn là một giáo viên tiếng Anh người Việt. Hãy cung cấp thông tin chi tiết cho từ vựng: '$word'.
            Trả về DUY NHẤT một đối tượng JSON với các trường sau (không được có markdown, không có code blocks):
            {
              "meaning": "Nghĩa tiếng Việt phổ biến nhất",
              "ipa": "Phiên âm quốc tế IPA",
              "example": "Một câu ví dụ tiếng Anh đơn giản sử dụng từ này",
              "type": "Chọn 1 trong các giá trị: NOUN, VERB, ADJECTIVE, ADVERB, PHRASE, OTHER"
            }
            Ví dụ cho từ 'apple': {"meaning": "quả táo", "ipa": "/ˈæpl/", "example": "I eat an apple every day.", "type": "NOUN"}
            Lưu ý quan trọng: 
            - Trường 'meaning' BẮT BUỘC phải là tiếng Việt.
            - Trường 'example' BẮT BUỘC phải có câu ví dụ tiếng Anh.
            - Trả về đúng định dạng JSON, không thêm bất kỳ văn bản giải thích nào khác.
        """.trimIndent()
        return generateContent(prompt)
    }

    suspend fun groupVocabularyByTopics(words: List<String>): String? {
        val prompt = """
            Bạn là một chuyên gia ngôn ngữ. Hãy phân tích danh sách từ vựng sau đây và phân loại chúng thành các chủ đề (topics) ý nghĩa để giúp người học dễ nhớ hơn.
            
            Danh sách từ: ${words.joinToString(", ")}
            
            Trả về DUY NHẤT một JSON array chứa các đối tượng có cấu trúc:
            - name: Tên chủ đề bằng tiếng Việt (ví dụ: "Di chuyển", "Ẩm thực", "Lưu trú", "Hành chính").
            - description: Một câu mô tả ngắn gọn về chủ đề bằng tiếng Việt.
            - words: Mảng các từ tiếng Anh (từ danh sách trên) thuộc về chủ đề này.
            
            Yêu cầu quan trọng:
            1. CHỈ sử dụng các từ có trong danh sách được cung cấp.
            2. Một từ có thể nằm trong nhiều bài học khác nhau nhưng chỉ nên thuộc về DUY NHẤT một nhóm chủ đề ở đây.
            3. Trả về đúng định dạng JSON, không có văn bản giải thích.
        """.trimIndent()
        return generateContent(prompt)
    }
}
