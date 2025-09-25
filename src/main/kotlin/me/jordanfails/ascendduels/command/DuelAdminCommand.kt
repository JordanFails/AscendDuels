package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.jordanfails.ascendduels.AscendDuels
import net.pvpwars.core.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("dueladmin")
@CommandPermission("duels.command.dueladmin")
class DuelAdminCommand : BaseCommand() {
    @Subcommand("end")
    @CommandCompletion("@players")
    fun onEnd(sender: CommandSender, @Flags("defaultself") @Optional @Default player: Player?) {
        if (player == null) {
            sender.sendMessage(CC.translate("&cPlayer not found."))
            return
        }
        
        val match = AscendDuels.instance.matchManagerV2.getMatch(player)
        if (match == null) {
            sender.sendMessage("§c${player.name} is not in a match.")
            return
        }
        
        match.forceEnd()
        sender.sendMessage("§aForced end of match for ${player.name}")
    }
    
    @Subcommand("stats")
    fun onStats(sender: CommandSender) {
        val stats = AscendDuels.instance.matchManagerV2.getStats()
        sender.sendMessage("§aMatch Manager Statistics:")
        sender.sendMessage("§f$stats")
    }
}