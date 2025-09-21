package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.arena.Arena
import me.jordanfails.ascendduels.kit.Kit
import me.jordanfails.ascendduels.kit.KitTag
import me.jordanfails.ascendduels.match.Match
import me.jordanfails.ascendduels.request.Request
import me.jordanfails.ascendduels.utils.WhitelistedItems
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.pvpwars.core.util.CombatTagUtil
import net.pvpwars.core.util.StringUtil
import net.pvpwars.core.util.item.ItemBuilder
import net.pvpwars.core.util.menu.Menu
import net.pvpwars.core.util.menu.button.Button
import net.pvpwars.core.util.menu.type.chest.ChestMenu
import net.pvpwars.core.util.menu.type.paginated.PaginatedMenu
import net.pvpwars.core.util.runnable.RunnableBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import co.aikar.commands.annotation.Optional
import me.jordanfails.ascendduels.command.menu.DuelsMenu

@CommandAlias("duel")
class DuelCommand : BaseCommand() {

    companion object {
        var DISABLED: Boolean = false
        val matchService = AscendDuels.instance.matchService
    }

    @Default
    @CommandCompletion("@players")
    fun onCommand(sender: Player, @Name("name") @Optional target: OnlinePlayer?) {
        if (DISABLED) {
            sender.sendMessage("${ChatColor.RED}AscendDuels are currently disabled!")
            return
        }
        if (isInCombat(sender)) {
            sender.sendMessage(AscendDuels.prefix("&cYou must be out of combat to duel."))
            return
        }

        if(target == null) {
            DuelsMenu().open(sender)
            return
        }

        if(sender.uniqueId == target.player.uniqueId) {
            sender.sendMessage(AscendDuels.prefix("&cYou can't duel yourself."))
            return
        }

        if(matchService.getByUuid(target.player.uniqueId)!= null) {
            sender.sendMessage(AscendDuels.prefix("&cThat player is already in a duel."))
            return
        }

        kitSelectMenu(sender, target.player)

    }

    @Subcommand("cancel")
    fun onCancel(sender: Player, requestID: String) {
        val request = AscendDuels.instance.requestService.removeRequest(UUID.fromString(requestID))
        if (request != null && request.sender == sender.uniqueId) {
            Bukkit.getPlayer(request.receiver)?.let {
                sender.sendMessage(AscendDuels.prefix("&fCancelled request to duel ${it.displayName}"))
            }
        } else {
            sender.sendMessage(AscendDuels.prefix("&cUnable to cancel this request."))
        }
    }

    @Subcommand("accept")
    fun onAccept(receiver: Player, requestID: String) {
        if (DISABLED) {
            receiver.sendMessage("${ChatColor.RED}AscendDuels are currently disabled!")
            return
        }
        if (isInCombat(receiver)) {
            receiver.sendMessage(AscendDuels.prefix("&cYou must be out of combat to duel."))
            return
        }
        receiver.closeInventory()

        val service = AscendDuels.instance.requestService
        val request = service.removeRequest(UUID.fromString(requestID))
        if (request != null && request.receiver == receiver.uniqueId) {
            val senderID = request.sender
            service.removeAllRequests(senderID)
            service.removeAllRequests(receiver.uniqueId)

            val sender = Bukkit.getPlayer(senderID) ?: return
            if (request.kit!!.hasTag(KitTag.RISK)) {
                val disallowedSender = WhitelistedItems.getDisallowedItems(sender)
                val disallowedReceiver = WhitelistedItems.getDisallowedItems(receiver)
                if (disallowedSender.isNotEmpty()) {
                    sender.sendMessage(AscendDuels.prefix("&cUnable to start the risk duel because you have disallowed items:"))
                    sender.sendMessage(
                        AscendDuels.prefix("&c" + disallowedSender.joinToString("&7, &c") {
                            it!!.type.name.lowercase().replace("_", " ")
                        })
                    )
                    receiver.sendMessage(AscendDuels.prefix("&cYour opponent has disallowed items."))
                    return
                }
                if (disallowedReceiver.isNotEmpty()) {
                    receiver.sendMessage(AscendDuels.prefix("&cUnable to start the risk duel because you have disallowed items:"))
                    receiver.sendMessage(
                        AscendDuels.prefix("&c" + disallowedReceiver.joinToString("&7, &c") {
                            it!!.type.name.lowercase().replace("_", " ")
                        })
                    )
                    sender.sendMessage(AscendDuels.prefix("&cYour opponent has disallowed items."))
                    return
                }

                sender.setMetadata("riskMatchConfirming", FixedMetadataValue(AscendDuels.instance, true))
                receiver.setMetadata("riskMatchConfirming", FixedMetadataValue(AscendDuels.instance, true))
                riskPreMatchConfirmMenu(request, sender, receiver)
            } else {
                startPlayerMatch(request, sender, receiver)
            }
        } else receiver.sendMessage(AscendDuels.prefix("&cUnable to accept this request."))
    }

    @Subcommand("deny")
    fun onDeny(receiver: Player, requestID: String) {
        if (DISABLED) {
            receiver.sendMessage("${ChatColor.RED}AscendDuels are currently disabled!")
            return
        }
        val request = AscendDuels.instance.requestService.removeRequest(UUID.fromString(requestID))
        if (request != null && request.receiver == receiver.uniqueId) {
            Bukkit.getPlayer(request.sender)?.let {
                receiver.sendMessage(AscendDuels.prefix("&fDenied request to duel ${it.displayName}"))
                it.sendMessage(AscendDuels.prefix("${receiver.displayName} &7denied your request."))
            }
        } else {
            receiver.sendMessage(AscendDuels.prefix("&cUnable to deny this request."))
        }
    }

    @Subcommand("disable")
    fun onDisable(player: Player) {
        DISABLED = !DISABLED
        player.sendMessage(if (DISABLED) "AscendDuels are now disabled!" else "AscendDuels are re-enabled!")
    }

    @Subcommand("leave")
    fun onLeave(player: Player) {
        AscendDuels.instance.matchService.getByPlayer(player)?.let { match ->
            match.onDeath(player, Match.DeathReason.QUIT)
            match.sendMessage(AscendDuels.prefix("&c${player.displayName} &fhas quit!"))
        }
    }

    private fun shouldNotStartMatch(sender: Player, receiver: Player): Boolean {
        return isInCombat(sender) || isInCombat(receiver) ||
                !sender.isOnline || !receiver.isOnline ||
                sender.isDead || receiver.isDead ||
                AscendDuels.instance.matchService.getByPlayer(sender) != null ||
                AscendDuels.instance.matchService.getByPlayer(receiver) != null
    }

    private fun startPlayerMatch(request: Request, sender: Player, receiver: Player) {
        receiver.sendMessage(AscendDuels.prefix("&fAccepted request to duel ${sender.displayName}"))

        if (shouldNotStartMatch(sender, receiver)) {
            sender.sendMessage(AscendDuels.prefix("&cUnable to start this match."))
            receiver.sendMessage(AscendDuels.prefix("&cUnable to start this match."))
            return
        }

        val match = AscendDuels.instance.matchService
            .newPlayerMatch(request.kit, request.arena, sender, receiver)
        if (match == null) {
            sender.sendMessage(AscendDuels.prefix("&cNo arena is available."))
            receiver.sendMessage(AscendDuels.prefix("&cNo arena is available."))
            return
        }
        match.start()
    }

    private fun startRiskMatch(request: Request, sender: Player, receiver: Player) {
        receiver.sendMessage(AscendDuels.prefix("&fAccepted request to duel ${sender.displayName}"))

        if (shouldNotStartMatch(sender, receiver)) {
            sender.sendMessage(AscendDuels.prefix("&cUnable to start this match."))
            receiver.sendMessage(AscendDuels.prefix("&cUnable to start this match."))
            return
        }

        val disallowedSender = WhitelistedItems.getDisallowedItems(sender)
        val disallowedReceiver = WhitelistedItems.getDisallowedItems(receiver)
        if (disallowedSender.isNotEmpty() || disallowedReceiver.isNotEmpty()) {
            disallowedSender.takeIf { it.isNotEmpty() }?.let {
                sender.sendMessage(AscendDuels.prefix("&cYou have disallowed items!"))
                sender.sendMessage(AscendDuels.prefix(it.joinToString("&7, &c") { i -> i!!.type.name }))
            }
            disallowedReceiver.takeIf { it.isNotEmpty() }?.let {
                receiver.sendMessage(AscendDuels.prefix("&cYou have disallowed items!"))
                receiver.sendMessage(AscendDuels.prefix(it.joinToString("&7, &c") { i -> i!!.type.name }))
            }
            return
        }

        val match = AscendDuels.instance.matchService
            .newRiskMatch(request.kit, request.arena, sender, receiver)
        if (match == null) {
            sender.sendMessage(AscendDuels.prefix("&cNo arena available."))
            receiver.sendMessage(AscendDuels.prefix("&cNo arena available."))
            return
        }
        match.start()
    }

    private fun kitSelectMenu(player: Player, target: Player) {



        val menu: Menu = PaginatedMenu("Select a Kit", 3, 7).apply {
            fillSides(Button.PLACEHOLDER)
            AscendDuels.instance.kitService.all()
                .filter { it.isEnabled }
                .sortedBy { it.name }
                .forEach { kit ->
                    addButton(Button(
                        ItemBuilder(kit.displayItem)
                            .lore("", "&7Click to select this kit.", "")
                            .flag(ItemFlag.HIDE_ENCHANTS)
                            .flag(ItemFlag.HIDE_POTION_EFFECTS)
                            .flag(ItemFlag.HIDE_ATTRIBUTES)
                            .flag(ItemFlag.HIDE_UNBREAKABLE)
                    ) { player1, _ -> arenaSelectMenu(player1, target, kit) })
                }
            buildInventory()
            show(player)
        }
    }

    private fun arenaSelectMenu(player: Player, target: Player, kit: Kit) {
        val menu: Menu = PaginatedMenu("Select an Arena", 3, 7).apply {
            fillSides(Button.PLACEHOLDER)
            AscendDuels.instance.arenaService.all()
                .filter { it.isComplete }
                .sortedBy { it.name }
                .forEach { arena ->
                    addButton(Button(
                        ItemBuilder(arena.displayItem!!)
                            .name("${ChatColor.DARK_RED}${arena.displayName}")
                            .lore("", "&aClick to select this arena.")
                            .flag(ItemFlag.HIDE_ENCHANTS)
                            .flag(ItemFlag.HIDE_POTION_EFFECTS)
                            .flag(ItemFlag.HIDE_ATTRIBUTES)
                            .flag(ItemFlag.HIDE_UNBREAKABLE),
                        { player2, _ -> duelRequest(player2, target, kit, arena) }
                    ))
                }
            buildInventory()
            show(player)
        }
    }

    private fun duelRequest(sender: Player, receiver: Player, kit: Kit, arena: Arena) {
        sender.closeInventory()
        val requestService = AscendDuels.instance.requestService

        if (requestService.hasExistingRequest(sender.uniqueId, receiver.uniqueId)) {
            sender.sendMessage(AscendDuels.prefix("&cYou already requested this player."))
            return
        }

        val request = requestService.createRequest(sender.uniqueId, receiver.uniqueId, kit, arena)
        sender.sendMessage("")
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lDuel Request Sent"))
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6┃ &fTo: &e${receiver.displayName}"))
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6┃ &fInfo: &e${kit.displayName} - ${arena.displayName}"))
        sender.sendMessage("")
        sender.spigot().sendMessage(*cancelMessage(request.id).create())
        sender.sendMessage("")

        receiver.sendMessage("")
        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lDuel Request"))
        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6┃ &fFrom: &e${sender.displayName}"))
        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6┃ &fInfo: &e${kit.displayName} - ${arena.displayName}"))
        receiver.sendMessage("")
        receiver.spigot().sendMessage(*acceptOrDenyMessage(request.id).create())
        receiver.sendMessage("")
    }

    private fun cancelMessage(requestID: UUID): ComponentBuilder {
        return ComponentBuilder(StringUtil.repeat(" ", StringUtil.getCenterSpaceCount("CANCEL")))
            .append("CANCEL").retain(ComponentBuilder.FormatRetention.NONE).color(ChatColor.RED).bold(true)
            .event(HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ComponentBuilder("Click to cancel the request").color(ChatColor.RED).create()))
            .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel cancel $requestID"))
    }

    private fun acceptOrDenyMessage(requestID: UUID): ComponentBuilder {
        return ComponentBuilder("")
            .append("ACCEPT").retain(ComponentBuilder.FormatRetention.NONE).color(ChatColor.GREEN).bold(true)
            .event(HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ComponentBuilder("Click to accept the request").color(ChatColor.GREEN).create()))
            .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept $requestID"))
            .append(" or ").retain(ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
            .append("DENY").color(ChatColor.RED).bold(true)
            .event(HoverEvent(HoverEvent.Action.SHOW_TEXT,
                ComponentBuilder("Click to deny the request").color(ChatColor.RED).create()))
            .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel deny $requestID"))
    }

    private fun riskPreMatchConfirmMenu(request: Request, sender: Player, receiver: Player) {
        val senderAccept = AtomicInteger(-1)
        val receiverAccept = AtomicInteger(-1)

        val senderMenu: Menu = buildInventoryMenu(sender).apply {
            setButton(50, Button(ItemBuilder(Material.WOOL, 5).name("&a&lAccept").lore("", "&7Click to accept"), { _, _ ->
                receiverAccept.set(1)
                receiver.closeInventory()
                if (senderAccept.get() != 0) {
                    sender.sendMessage(AscendDuels.prefix("&fRisk duel accepted by ${receiver.displayName}"))
                    receiver.sendMessage(AscendDuels.prefix("&fRisk duel accepted by ${receiver.displayName}"))
                }
            }))
            setButton(48, Button(ItemBuilder(Material.WOOL, 14).name("&c&lDeny").lore("", "&7Click to deny"), { _, _ ->
                receiverAccept.set(0)
                receiver.closeInventory()
            }))
            setCloseCallback { _, _ -> if (receiverAccept.get() == -1) receiverAccept.set(0) }
            buildInventory(); show(receiver)
        }

        val receiverMenu: Menu = buildInventoryMenu(receiver).apply {
            setButton(50, Button(ItemBuilder(Material.WOOL, 5).name("&a&lAccept").lore("", "&7Click to accept"), { _, _ ->
                senderAccept.set(1); sender.closeInventory()
                if (receiverAccept.get() != 0) {
                    sender.sendMessage(AscendDuels.prefix("&fRisk duel accepted by ${sender.displayName}"))
                    receiver.sendMessage(AscendDuels.prefix("&fRisk duel accepted by ${sender.displayName}"))
                }
            }))
            setButton(48, Button(ItemBuilder(Material.WOOL, 14).name("&c&lDeny").lore("", "&7Click to deny"), { _, _ ->
                senderAccept.set(0); sender.closeInventory()
            }))
            setCloseCallback { _, _ -> if (senderAccept.get() == -1) senderAccept.set(0) }
            buildInventory(); show(sender)
        }

        object : BukkitRunnable() {
            override fun run() {
                if (!sender.isOnline || !receiver.isOnline || sender.isDead || receiver.isDead) {
                    cancel()
                    val msg = AscendDuels.prefix("&cThe risk duel was cancelled.")
                    sender.sendMessage(msg); receiver.sendMessage(msg)
                    sender.removeMetadata("riskMatchConfirming", AscendDuels.instance)
                    receiver.removeMetadata("riskMatchConfirming", AscendDuels.instance)
                }
                if (senderAccept.get() == 0) {
                    cancel()
                    val msg = AscendDuels.prefix("&cDenied by ${sender.displayName}")
                    sender.sendMessage(msg); receiver.sendMessage(msg)
                    sender.removeMetadata("riskMatchConfirming", AscendDuels.instance)
                    receiver.removeMetadata("riskMatchConfirming", AscendDuels.instance)
                } else if (receiverAccept.get() == 0) {
                    cancel()
                    val msg = AscendDuels.prefix("&cDenied by ${receiver.displayName}")
                    sender.sendMessage(msg); receiver.sendMessage(msg)
                    sender.removeMetadata("riskMatchConfirming", AscendDuels.instance)
                    receiver.removeMetadata("riskMatchConfirming", AscendDuels.instance)
                }
                if (senderAccept.get() == 1 && receiverAccept.get() == 1) {
                    cancel()
                    RunnableBuilder.forPlugin(AscendDuels.instance).with {
                        startRiskMatch(request, sender, receiver)
                        sender.removeMetadata("riskMatchConfirming", AscendDuels.instance)
                        receiver.removeMetadata("riskMatchConfirming", AscendDuels.instance)
                    }.runSync()
                }
            }
        }.runTaskTimerAsynchronously(AscendDuels.instance, 1L, 1L)
    }

    private fun buildInventoryMenu(player: Player): Menu {
        val menu = ChestMenu("Opponent's Inventory", 6)
        val contents = player.inventory.contents
        val armor = player.inventory.armorContents
        for (i in 0 until 9) menu.setButton(27 + i, contents[i])
        for (i in 9 until contents.size) menu.setButton(i - 9, contents[i])
        for (i in armor.indices) menu.setButton(39 - i, armor[i])
        return menu
    }

    fun isInCombat(player: Player): Boolean {
        return CombatTagUtil.isTagged(player)
    }
}