package me.jordanfails.ascendduels.api.serializable.builder

import com.google.gson.JsonElement
import com.google.gson.JsonObject

class JsonObjectBuilder {
    val jsonObject: JsonObject = JsonObject()

    fun addProperty(key: String, value: JsonElement?): JsonObjectBuilder {
        jsonObject.add(key, value)
        return this
    }

    fun addProperty(key: String, value: String?): JsonObjectBuilder {
        jsonObject.addProperty(key, value)
        return this
    }

    fun addProperty(key: String, value: Boolean?): JsonObjectBuilder {
        jsonObject.addProperty(key, value)
        return this
    }

    fun addProperty(key: String, value: Number?): JsonObjectBuilder {
        jsonObject.addProperty(key, value)
        return this
    }

    fun addProperty(key: String, value: Char?): JsonObjectBuilder {
        jsonObject.addProperty(key, value)
        return this
    }
}