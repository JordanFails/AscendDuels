package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.jordanfails.ascendduels.AscendDuels
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("duelinventory")
class DuelInventoryCommand : BaseCommand() {
    
    @Default
    fun onCommand(player: Player, @Name("uuid") uuidString: String) {
        try {
            val uuid = UUID.fromString(uuidString)
            val matchInventory = AscendDuels.instance.matchInventoryService.get(uuid)
            
            if (matchInventory == null) {
                player.sendMessage(AscendDuels.prefix("&cThis inventory is no longer valid."))
                return
            }

            matchInventory.openMenu(player)
        } catch (ex: Exception) {
            ex.printStackTrace()
            player.sendMessage(AscendDuels.prefix("&cInvalid UUID."))
        }
    }
}