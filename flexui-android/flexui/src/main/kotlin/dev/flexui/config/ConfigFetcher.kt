package dev.flexui.config

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Fetches FlexUI configurations from remote server using HttpURLConnection (no external dependencies)
 */
class ConfigFetcher(
    private val baseUrl: String,
    private val connectTimeoutMs: Long = 10_000,
    private val readTimeoutMs: Long = 30_000
) {
    
    /**
     * Fetch configuration for a specific screen
     */
    suspend fun fetchConfig(screenId: String, params: Map<String, String> = emptyMap()): ConfigResult {
        return try {
            val url = buildUrl(screenId, params)
            val response = performRequest(url)
            
            if (response.isSuccessful) {
                ConfigResult.Success(response.body)
            } else {
                ConfigResult.Error("HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            ConfigResult.Error("Network error: ${e.message}")
        }
    }
    
    /**
     * Fetch configuration synchronously (for blocking calls)
     */
    fun fetchConfigSync(screenId: String, params: Map<String, String> = emptyMap()): ConfigResult {
        return try {
            val url = buildUrl(screenId, params)
            val response = performRequest(url)
            
            if (response.isSuccessful) {
                ConfigResult.Success(response.body)
            } else {
                ConfigResult.Error("HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            ConfigResult.Error("Network error: ${e.message}")
        }
    }
    
    private fun buildUrl(screenId: String, params: Map<String, String>): String {
        val encodedScreenId = URLEncoder.encode(screenId, "UTF-8")
        val baseUrl = "${this.baseUrl.trimEnd('/')}/screens/$encodedScreenId"
        
        if (params.isEmpty()) {
            return baseUrl
        }
        
        val queryString = params.entries.joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }
        
        return "$baseUrl?$queryString"
    }
    
    private fun performRequest(urlString: String): HttpResponse {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            // Configure connection
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeoutMs.toInt()
            connection.readTimeout = readTimeoutMs.toInt()
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "FlexUI-Android/1.0")
            
            // Connect and get response
            connection.connect()
            
            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage ?: ""
            
            val inputStream = if (responseCode >= 400) {
                connection.errorStream ?: connection.inputStream
            } else {
                connection.inputStream
            }
            
            val responseBody = inputStream?.use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } ?: ""
            
            return HttpResponse(
                code = responseCode,
                message = responseMessage,
                body = responseBody,
                isSuccessful = responseCode in 200..299
            )
        } finally {
            connection.disconnect()
        }
    }
    
    companion object {
        /**
         * Create a ConfigFetcher with default settings
         */
        @JvmStatic
        fun create(baseUrl: String): ConfigFetcher {
            return ConfigFetcher(baseUrl)
        }
        
        /**
         * Create a ConfigFetcher with custom timeouts
         */
        @JvmStatic
        fun create(
            baseUrl: String,
            connectTimeoutMs: Long,
            readTimeoutMs: Long
        ): ConfigFetcher {
            return ConfigFetcher(baseUrl, connectTimeoutMs, readTimeoutMs)
        }
    }
}

/**
 * HTTP response data
 */
data class HttpResponse(
    val code: Int,
    val message: String,
    val body: String,
    val isSuccessful: Boolean
)

/**
 * Result of config fetching operation
 */
sealed class ConfigResult {
    data class Success(val json: String) : ConfigResult()
    data class Error(val message: String) : ConfigResult()
    
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    
    fun getJsonOrNull(): String? = if (this is Success) json else null
    fun getErrorOrNull(): String? = if (this is Error) message else null
}

/**
 * Configuration parameters for fetching
 */
data class FetchParams(
    val version: String? = null,
    val platform: String = "android",
    val locale: String? = null,
    val userId: String? = null,
    val sessionId: String? = null,
    val customParams: Map<String, String> = emptyMap()
) {
    fun toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        
        version?.let { map["version"] = it }
        map["platform"] = platform
        locale?.let { map["locale"] = it }
        userId?.let { map["userId"] = it }
        sessionId?.let { map["sessionId"] = it }
        map.putAll(customParams)
        
        return map
    }
}