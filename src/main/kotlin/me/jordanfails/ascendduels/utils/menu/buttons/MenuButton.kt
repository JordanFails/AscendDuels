package me.jordanfails.ascendduels.utils.menu.buttons

import me.jordanfails.ascendduels.utils.menu.Button
import net.atlantismc.menus.item.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.*

open class MenuButton : Button() {

    internal var icon: ItemStack? = null
    internal var getIcon: ((Player) -> ItemStack)? = null

    internal var name: String? = null
    internal var getName: ((Player) -> String)? = null

    internal var lore: List<String>? = null
    internal var getLore: ((Player) -> List<String>)? = null

    internal var action: ((Player, ClickType) -> Unit)? = null
    internal val actions: MutableMap<ClickType, ((Player) -> Unit)> = EnumMap(ClickType::class.java)

    internal var glow: Boolean = false
    internal var isGlow: ((Player) -> Boolean)? = null

    fun icon(itemStack: ItemStack): MenuButton {
        this.icon = itemStack.clone()
        return this
    }

    fun icon(material: Material): MenuButton {
        this.icon = ItemStack(material)
        return this
    }

    fun icon(getIcon: (Player) -> ItemStack): MenuButton {
        this.getIcon = getIcon
        return this
    }

    fun name(name: String): MenuButton {
        this.name = name
        return this
    }

    fun name(getName: (Player) -> String): MenuButton {
        this.getName = getName
        return this
    }

    fun lore(lore: List<String>): MenuButton {
        this.lore = lore
        return this
    }

    fun lore(getLore: (Player) -> List<String>): MenuButton {
        this.getLore = getLore
        return this
    }

    fun action(action: (Player, ClickType) -> Unit): MenuButton {
        this.action = action
        return this
    }

    fun action(clickType: ClickType, action: (Player) -> Unit): MenuButton {
        actions[clickType] = action
        return this
    }

    fun action(action: (Player) -> Unit, vararg clickTypes: ClickType): MenuButton {
        for (clickType in clickTypes) {
            actions[clickType] = action
        }
        return this
    }

    fun glow(glow: Boolean): MenuButton {
        this.glow = glow
        return this
    }

    fun glow(isGlow: (Player) -> Boolean): MenuButton {
        this.isGlow = isGlow
        return this
    }

    override fun getButtonItem(player: Player): ItemStack {
        if (icon == null && getIcon == null) {
            return ItemStack(Material.AIR)
        }

        val item = if (getIcon != null) {
            getIcon!!.invoke(player)
        } else {
            icon!!.clone()
        }

        val itemMeta = item.itemMeta

        if (getName != null) {
            itemMeta.displayName = getName!!.invoke(player)
        } else if (name != null) {
            itemMeta.displayName = name
        }

        if (getLore != null) {
            itemMeta.lore = getLore!!.invoke(player)
        } else if (lore != null) {
            itemMeta.lore = lore
        }

        item.itemMeta = itemMeta

        if (glow || this.isGlow?.invoke(player) == true) {
            ItemBuilder(item).enchant(org.bukkit.enchantments.Enchantment.LURE, 1)
        }

        return item
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        if (action != null) {
            action!!.invoke(player, clickType)
        } else {
            if (actions.containsKey(clickType)) {
                actions[clickType]!!.invoke(player)
            }
        }
    }

}