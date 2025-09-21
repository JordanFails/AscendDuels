package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import me.jordanfails.ascendduels.AscendDuels
import org.bukkit.entity.Player

@CommandAlias("duelspy")
@CommandPermission("group.staff.mod")
class DuelSpyCommand : BaseCommand() {

    @Default
    fun onCommand(player: Player) {
        val toggledOn = AscendDuels.instance.duelSpyService.toggleSpying(player)
        val state = if (toggledOn) "&aON" else "&cOFF"
        player.sendMessage(AscendDuels.prefix("Duel spying toggled $state"))
    }
}