package me.jordanfails.ascendduels.api.serializable

interface Serializable<T> {
    fun serialize(): T?
    fun serializeToString(): String?

    fun deserialize(jsonObject: T?)
}