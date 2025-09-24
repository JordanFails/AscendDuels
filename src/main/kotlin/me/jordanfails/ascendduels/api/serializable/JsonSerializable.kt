package me.jordanfails.ascendduels.api.serializable

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.jordanfails.ascendduels.utils.JsonUtil

interface JsonSerializable : Serializable<JsonObject?> {
    public override fun serializeToString(): String {
        return serialize().toString()
    }

    /**
     * Serializes to a pretty-printed JSON string
     */
    fun serializeToPrettyString(): String {
        return serialize()?.let { JsonUtil.toPrettyString(it) } ?: "{}"
    }

    fun deserialize(string: String?) {
        if (string == null || string.trim().isEmpty()) {
            return
        }
        try {
            deserialize(JSON_PARSER.parse(string).asJsonObject)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse JSON: ${e.message}", e)
        }
    }


    companion object {
        val JSON_PARSER: JsonParser = JsonParser()
    }
}