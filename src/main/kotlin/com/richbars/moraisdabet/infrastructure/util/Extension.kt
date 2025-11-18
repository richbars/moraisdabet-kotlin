package com.richbars.moraisdabet.infrastructure.util
import okhttp3.Response
import org.json.JSONObject

fun Response.toJson(): JSONObject {
    val bodyString = this.body?.string()

    if (bodyString.isNullOrBlank()) {
        return JSONObject()
    }

    return try {
        JSONObject(bodyString)
    } catch (e: Exception) {
        JSONObject().put("error", "Invalid JSON").put("raw", bodyString)
    }
}