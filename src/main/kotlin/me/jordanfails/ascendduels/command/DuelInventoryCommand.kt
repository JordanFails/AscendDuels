package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.inventory.MatchInventory
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("duelinventory")
class DuelInventoryCommand : BaseCommand() {
    @Default
    fun onCommand(player: Player, uuidString: String) {
        try {
            val uuid = UUID.fromString(uuidString)
            val matchInventory: MatchInventory? = AscendDuels.instance.matchInventoryService.get(uuid)
            if (matchInventory == null) {
                player.sendMessage(AscendDuels.prefix("&cThis inventory is no longer valid."))
                return
            }

            matchInventory.open(player)
        } catch (ex: Exception) {
            ex.printStackTrace()
            player.sendMessage(AscendDuels.prefix("&cInvalid UUID."))
        }
    }
}