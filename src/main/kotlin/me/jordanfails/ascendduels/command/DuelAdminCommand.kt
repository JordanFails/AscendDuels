package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.jordanfails.ascendduels.AscendDuels
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("dueladmin")
@CommandPermission("duels.command.dueladmin")
class DuelAdminCommand : BaseCommand() {
    @Subcommand("end")
    @CommandCompletion("@players")
    fun onEnd(sender: CommandSender?, @Flags("defaultself") @Optional @Default() player: Player?) {
        AscendDuels.instance.matchService.getByPlayer(player!!)!!.end()
    }
}