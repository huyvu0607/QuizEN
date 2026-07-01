package com.lumina.app.data.source.remote.ai

import retrofit2.http.*

interface GeminiApiService {
    @POST("v1/models/{model}:generateContent")
    suspend fun generateContentV1(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContentBeta(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    @GET("v1beta/models")
    suspend fun listModels(
        @Query("key") apiKey: String
    ): ModelListResponse
}

data class GeminiRequest(val contents: List<Content>)
data class Content(val parts: List<Part>)
data class Part(val text: String)

data class GeminiResponse(val candidates: List<Candidate>)
data class Candidate(val content: ContentResponse)
data class ContentResponse(val parts: List<Part>)

data class ModelListResponse(val models: List<ModelInfo>)
data class ModelInfo(
    val name: String,
    val version: String,
    val displayName: String,
    val supportedGenerationMethods: List<String>?
)
