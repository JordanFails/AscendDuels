package me.jordanfails.ascendduels.api.serializable.inventory

import com.google.gson.JsonObject
import me.jordanfails.ascendduels.api.serializable.JsonSerializable
import me.jordanfails.ascendduels.api.serializable.builder.JsonObjectBuilder
import net.pvpwars.duels.util.SerializationUtil
import org.bukkit.inventory.ItemStack

open class JsonInventory(
    var contents: Array<ItemStack?> = emptyArray(),
    var armorContents: Array<ItemStack?> = emptyArray()
) : JsonSerializable {

    override fun serialize(): JsonObject {
        return JsonObjectBuilder()
            .addProperty(
                "contents",
                SerializationUtil.serializeCollection(contents.toList()) ?: ""
            )
            .addProperty(
                "armorContents",
                SerializationUtil.serializeCollection(armorContents.toList()) ?: ""
            )
            .jsonObject
    }

    override fun deserialize(jsonObject: JsonObject?) {
        if (jsonObject == null) return

        if (jsonObject.has("contents") && !jsonObject["contents"].isJsonNull) {
            val deserialized = SerializationUtil.deserializeCollection(jsonObject["contents"].asString)
            contents = if (deserialized != null) {
                deserialized.map { it as? ItemStack }.toTypedArray()
            } else {
                emptyArray()
            }
        }

        if (jsonObject.has("armorContents") && !jsonObject["armorContents"].isJsonNull) {
            val deserialized = SerializationUtil.deserializeCollection(jsonObject["armorContents"].asString)
            armorContents = if (deserialized != null) {
                deserialized.map { it as? ItemStack }.toTypedArray()
            } else {
                emptyArray()
            }
        }
    }
}