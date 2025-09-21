package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.inventory.RiskPostMatchInventory
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("riskmatchloot")
class RiskMatchLootCommand : BaseCommand() {
    @Default
    fun onCommand(player: Player, uuidString: String) {
        try {
            val uuid = UUID.fromString(uuidString)
            val riskPostMatchInventory: RiskPostMatchInventory? =
                AscendDuels.instance.riskPostMatchInventoryService.get(uuid)
            if (riskPostMatchInventory == null) {
                player.sendMessage(AscendDuels.prefix("&cThis inventory is no longer valid."))
                return
            }

            player.openInventory(riskPostMatchInventory.inventory)
        } catch (ex: Exception) {
            ex.printStackTrace()
            player.sendMessage(AscendDuels.prefix("&cInvalid UUID."))
        }
    }
}