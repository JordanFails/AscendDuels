package me.jordanfails.ascendduels.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement

object JsonUtil {
    /**
     * Creates a Gson instance configured for pretty printing
     */
    val prettyGson: Gson by lazy {
        GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .create()
    }

    /**
     * Converts a JsonElement to a pretty-printed JSON string
     */
    fun toPrettyString(jsonElement: JsonElement): String {
        return prettyGson.toJson(jsonElement)
    }

    /**
     * Converts any object to a pretty-printed JSON string
     */
    fun toPrettyString(obj: Any): String {
        return prettyGson.toJson(obj)
    }
}
