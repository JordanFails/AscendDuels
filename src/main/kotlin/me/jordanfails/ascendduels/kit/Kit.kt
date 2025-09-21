package me.jordanfails.ascendduels.kit

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import me.jordanfails.ascendduels.api.serializable.JsonSerializable
import me.jordanfails.ascendduels.api.serializable.builder.JsonObjectBuilder
import net.pvpwars.core.Core
import net.pvpwars.core.util.StringUtil
import net.pvpwars.core.util.item.ItemBuilder
import net.pvpwars.duels.util.SerializationUtil
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Kit() : JsonSerializable {

    var name: String? = null
    var disabled: Boolean = false
    private var _displayName: String? = null
    private var _displayItem: ItemStack? = null
    var inventory: KitInventory? = null
    var tags: MutableSet<KitTag> = HashSet()

    constructor(json: String) : this() {
        deserialize(json)
    }

    /** Colored display name */
    val displayName: String
        get() = _displayName?.let { StringUtil.color(it) } ?: (name ?: "")

    /** Display item (fallback to sponge if unset) */
    val displayItem: ItemStack
        get() = _displayItem ?: ItemBuilder(Material.SPONGE).name(displayName)

    val isEnabled: Boolean
        get() = !disabled

    val isComplete: Boolean
        get() = _displayName != null && _displayItem != null && inventory != null

    fun hasTag(tag: KitTag) = tags.contains(tag)

    /** Mutators for editing kits */
    fun setDisplayName(displayName: String) {
        _displayName = displayName
    }

    fun setDisplayItem(stack: ItemStack?) {
        _displayItem = stack
    }

    override fun serialize(): JsonObject {
        val gson: Gson = Core.getInstance().gson
        return JsonObjectBuilder()
            .addProperty("name", name)
            .addProperty("disabled", disabled)
            .addProperty("displayName", _displayName)
            .addProperty("displayItem", SerializationUtil.itemStackToBase64(_displayItem))
            .addProperty("inventory", inventory?.serializeToString() ?: "")
            .addProperty("tags", gson.toJsonTree(tags))
            .jsonObject
    }

    override fun deserialize(json: JsonObject?) {
        if (json == null) return
        val gson: Gson = Core.getInstance().gson

        name = json["name"].asString
        disabled = json["disabled"].asBoolean
        _displayName = json["displayName"].asString

        val displayItemStr = json["displayItem"].asString
        if (displayItemStr.isNotEmpty()) {
            _displayItem = SerializationUtil.itemStackFromBase64(displayItemStr)
        }

        inventory = KitInventory().apply {
            deserialize(json["inventory"].asString)
        }

        tags = if (json.has("tags")) {
            gson.fromJson(
                json["tags"].asJsonArray,
                object : TypeToken<Set<KitTag>>() {}.type
            )
        } else {
            HashSet()
        }
    }

    companion object {
        val EMPTY: Kit = Kit().apply { inventory = KitInventory() }
    }
}