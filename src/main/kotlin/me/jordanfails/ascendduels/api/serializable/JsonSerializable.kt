package me.jordanfails.ascendduels.api.serializable

import com.google.gson.JsonObject
import com.google.gson.JsonParser

interface JsonSerializable : Serializable<JsonObject?> {
    public override fun serializeToString(): String {
        return serialize().toString()
    }

    fun deserialize(string: String?) {
        deserialize(JSON_PARSER.parse(string).asJsonObject)
    }


    companion object {
        val JSON_PARSER: JsonParser = JsonParser()
    }
}