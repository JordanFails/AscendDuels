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
            .addProperty("contents", SerializationUtil.itemStackArrayToBase64(contents))
            .addProperty("armorContents", SerializationUtil.itemStackArrayToBase64(armorContents))
            .jsonObject
    }

    override fun deserialize(t: JsonObject?) {
        if (t == null) return

        contents = SerializationUtil.itemStackArrayFromBase64(
            t.get("contents").asString
        )
        armorContents = SerializationUtil.itemStackArrayFromBase64(
            t.get("armorContents").asString
        )
    }
}