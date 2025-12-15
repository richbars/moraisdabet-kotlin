package com.richbars.moraisdabet.infrastructure.adapter.out

import com.richbars.moraisdabet.core.application.port.PackballHttpPort
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PackballHttpAdapter(
    @Value("\${packball.email}")
    private val email: String,
    @Value("\${packball.password}")
    private val password: String
) : PackballHttpPort {

    companion object {
        val client = OkHttpClient()
    }

    override suspend fun login(): String {

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = "{\"email\":\"$email\",\"password\":\"$password\"}".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api-pb.azurewebsites.net/users/authenticate")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val bodyString = response.body?.string()
        val json = JSONObject(bodyString)

        return json.optJSONObject("data").optString("token")
    }
}