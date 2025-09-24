package me.jordanfails.ascendduels.kit

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import me.jordanfails.ascendduels.AscendDuels
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

    override fun deserialize(jsonObject: JsonObject?) {
        if (jsonObject == null) return
        val gson: Gson = Core.getInstance().gson

        name = jsonObject["name"].asString
        disabled = jsonObject["disabled"].asBoolean
        _displayName = jsonObject["displayName"].asString

        val displayItemStr = jsonObject["displayItem"].asString
        if (displayItemStr.isNotEmpty()) {
            try {
                _displayItem = SerializationUtil.itemStackFromBase64(displayItemStr)
            } catch (e: Exception) {
                AscendDuels.instance.logger.warning("Failed to deserialize display item for kit '${name}': ${e.message}")
                _displayItem = null
            }
        }

        inventory = KitInventory().apply {
            try {
                deserialize(jsonObject["inventory"].asString)
            } catch (e: Exception) {
                AscendDuels.instance.logger.warning("Failed to deserialize inventory for kit '${name}': ${e.message}")
                // Keep the empty inventory
            }
        }

        tags = if (jsonObject.has("tags")) {
            gson.fromJson(
                jsonObject["tags"].asJsonArray,
                object : TypeToken<Set<KitTag>>() {}.type
            )
        } else {
            HashSet()
        }
    }

    companion object {
        val EMPTY: Kit = Kit().apply { inventory = KitInventory() }

        val DEFAULT: Kit = Kit().apply {
            name = "Default"
            setDisplayName("&aDefault Kit")
            tags = mutableSetOf(KitTag.NORMAL)
            setDisplayItem(ItemStack(Material.DIAMOND_SWORD))
            inventory = KitInventory(
                arrayOf(ItemStack(Material.DIAMOND_SWORD), ItemStack(Material.GOLDEN_APPLE, 5)),
                arrayOf(ItemStack(Material.DIAMOND_HELMET), ItemStack(Material.DIAMOND_CHESTPLATE), ItemStack(Material.DIAMOND_LEGGINGS), ItemStack(Material.IRON_BOOTS))
            )
        }
    }
}