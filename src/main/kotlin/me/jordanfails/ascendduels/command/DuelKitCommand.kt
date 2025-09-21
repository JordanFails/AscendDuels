package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.kit.Kit
import me.jordanfails.ascendduels.kit.KitInventory
import me.jordanfails.ascendduels.kit.KitService
import me.jordanfails.ascendduels.kit.KitTag
import net.pvpwars.core.util.item.ItemBuilder
import net.pvpwars.core.util.item.ItemNames
import net.pvpwars.core.util.menu.Menu
import net.pvpwars.core.util.menu.button.Button
import net.pvpwars.core.util.menu.type.chest.ChestMenu
import net.pvpwars.core.util.menu.type.paginated.PaginatedMenu
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

@CommandAlias("duelkit|dkit")
@CommandPermission("command.duels.admin")
class DuelKitCommand : BaseCommand() {

    private val kitService: KitService = AscendDuels.instance.kitService

    @Default
    fun onCommand(sender: CommandSender) {
        sender.sendMessage(
            AscendDuels.prefix(
                "/duelkit <list;create;delete;toggle;displayname;displayitem;inventory;tag> (kit)"
            )
        )
    }

    @Subcommand("list")
    fun onList(sender: CommandSender) {
        if (sender is Player) {
            val menu: Menu = PaginatedMenu("All Kits", 3, 7).apply {
                fillSides(Button.PLACEHOLDER)
                kitService.all()
                    .sortedBy { it.name }
                    .forEach { kit ->
                        addButton(Button(
                            {
                                ItemBuilder(kit.displayItem)
                                    .lore(
                                        " ",
                                        "&fLeft click &7to preview this kit.",
                                        "&fRight click &7to load this kit.",
                                        "&fMiddle click &7to ${if (kit.disabled) "enable" else "disable"} this kit.",
                                        "&fShift + Right click &7to delete this kit.",
                                        " "
                                    )
                            },
                            { player, info ->
                                when (info.clickType) {
                                    ClickType.LEFT -> {
                                        if (kit.inventory == null) {
                                            player.sendMessage(AscendDuels.prefix("&cThis kit does not have an inventory set! &c&o(/kit inventory ${kit.name})"))
                                            return@Button
                                        }

                                        val preview = ChestMenu("${kit.displayName} &7Preview", 54) { _, p -> show(p) }
                                        val contents = kit.inventory!!.contents
                                        for (i in 0 until 9) preview.setButton(27 + i, contents[i])
                                        for (i in 9 until contents.size) {
                                            preview.setButton(i - 9, contents[i])
                                        }

                                        val armor = kit.inventory!!.armorContents
                                        for (i in 0 until 4) preview.setButton(48 - i, armor[i])

                                        preview.setButton(53, Button(
                                            ItemBuilder(Material.BARRIER).name("&c&lGo Back")
                                                .lore("${ChatColor.GRAY}Click here to return")
                                        ) { _, _ -> show(player) })

                                        preview.show(player)
                                    }

                                    ClickType.RIGHT -> {
                                        if (kit.inventory == null) {
                                            player.sendMessage(AscendDuels.prefix("&cThis kit does not have an inventory set! &c&o(/kit inventory ${kit.name})"))
                                            return@Button
                                        }
                                        player.closeInventory()
                                        kit.inventory!!.load(player)
                                    }

                                    ClickType.MIDDLE -> {
                                        onToggle(player, kit)
                                        show(player)
                                    }

                                    ClickType.SHIFT_RIGHT -> {
                                        onDelete(player, kit)
                                        show(player)
                                    }

                                    else -> {}
                                }
                            }
                        ))
                    }
                buildInventory()
                show(sender)
            }
        } else {
            val kits = kitService.all().sortedBy { it.name }.map { it.displayName }
            sender.sendMessage(
                AscendDuels.prefix("All kits (${kits.size}): ${kits.joinToString("&f, &7")}")
            )
        }
    }

    @Subcommand("create")
    fun onCreate(sender: CommandSender, name: String) {
        if (kitService.get(name) != null) {
            sender.sendMessage(AscendDuels.prefix("&cA kit by the name '$name' already exists."))
            return
        }
        val kit = Kit().apply { this.name = name }
        kitService.add(kit)
        kitService.saveKits()
        sender.sendMessage(AscendDuels.prefix("Successfully created the kit '${kit.name}'."))
    }

    @Subcommand("delete")
    @CommandCompletion("@duelKits")
    fun onDelete(sender: CommandSender, kit: Kit) {
        kitService.remove(kit.name!!)
        kitService.saveKits()
        sender.sendMessage(AscendDuels.prefix("Successfully deleted kit '${kit.displayName}&7'."))
    }

    @Subcommand("toggle")
    @CommandCompletion("@duelKits")
    fun onToggle(sender: CommandSender, kit: Kit) {
        kit.disabled = !kit.disabled
        kitService.saveKits()
        sender.sendMessage(
            AscendDuels.prefix(
                "Successfully ${if (kit.disabled) "&cdisabled" else "&aenabled"} &fthe kit '${kit.displayName}&7'."
            )
        )
    }

    @Subcommand("displayName")
    @CommandCompletion("@duelKits")
    fun onDisplayName(sender: CommandSender, kit: Kit, displayName: String) {
        kit.setDisplayName(displayName)
        kit.setDisplayItem(ItemBuilder(kit.displayItem).name(displayName))
        kitService.saveKits()
        sender.sendMessage(AscendDuels.prefix("Set kit '${kit.name}' display name to '$displayName&7'."))
    }

    @Subcommand("displayItem")
    @CommandCompletion("@duelKits")
    fun onDisplayItem(@Flags("itemheld") player: Player, kit: Kit) {
        val held = player.itemInHand
        kit.setDisplayItem(held)
        kit.setDisplayName(if(kit.name == null) "Kit" else kit.name!!)
        kitService.saveKits()
        player.sendMessage(
            AscendDuels.prefix("Set display of kit '${kit.name}' to '${ItemNames.lookup(held)}'.")
        )
    }

    @Subcommand("inventory")
    @CommandCompletion("@duelKits")
    fun onInventory(player: Player, kit: Kit) {
        kit.inventory = KitInventory(
            player.inventory.contents,
            player.inventory.armorContents
        )
        kitService.saveKits()
        player.sendMessage(AscendDuels.prefix("Set inventory of kit '${kit.displayName}&7' to your current inv."))
    }

    @Subcommand("tag")
    @CommandCompletion("@duelKits")
    fun onTag(sender: CommandSender, kit: Kit, kitTag: KitTag) {
        if (!kit.tags.add(kitTag)) {
            kit.tags.remove(kitTag)
            sender.sendMessage(AscendDuels.prefix("Removed tag ${kitTag.name} from kit ${kit.displayName}"))
        } else {
            sender.sendMessage(AscendDuels.prefix("Added tag ${kitTag.name} to kit ${kit.displayName}"))
        }
        kitService.saveKits()
    }

    @Subcommand("listTags")
    @CommandCompletion("@duelKits")
    fun onListTags(sender: CommandSender, kit: Kit) {
        sender.sendMessage(AscendDuels.prefix("&fTags for ${kit.name}"))
        sender.sendMessage(kit.tags.joinToString(", ") { it.name })
    }
}