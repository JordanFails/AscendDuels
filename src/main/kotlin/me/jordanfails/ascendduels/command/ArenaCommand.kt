package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import com.massivecraft.factions.util.CC
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.arena.Arena
import me.jordanfails.ascendduels.arena.ArenaGrid
import me.jordanfails.ascendduels.arena.ArenaSchematic
import me.jordanfails.ascendduels.utils.LocationUtils
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.io.File

@CommandAlias("arena")
@Description("Manage PotPvP arenas")
@CommandPermission("potpvp.arena.admin")
class ArenaCommand : BaseCommand() {

    companion object {
//        private val HELP_MESSAGE = arrayOf(
//            "${ChatColor.DARK_PURPLE}${PotPvPLang.LONG_LINE}",
//            "§5§lArena Commands",
//            "${ChatColor.DARK_PURPLE}${PotPvPLang.LONG_LINE}",
//            "§c ${PotPvPLang.LEFT_ARROW_NAKED} §a/arena free",
//            "§c ${PotPvPLang.LEFT_ARROW_NAKED} §a/arena createSchematic <schematic>",
//            "§c ${PotPvPLang.LEFT_ARROW_NAKED} §a/arena listArenas <schematic>",
//            "§c ${PotPvPLang.LEFT_ARROW_NAKED} §a/arena repasteSchematic <schematic>",
//            "§c ${PotPvPLang.LEFT_ARROW_NAKED} §a/arena rescaleall",
//            "§c ${PotPvPLang.LEFT_ARROW_NAKED} §a/arena listSchematics",
//            "${ChatColor.DARK_PURPLE}${this.LONG_LINE}"
//        )

        val LONG_LINE: String = "${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-", 53)}"
    }

//    @Default
//    fun onHelp(player: Player) {
//        player.sendMessage(HELP_MESSAGE)
//    }

    @Subcommand("free")
    @Description("Free arena grid")
    fun arenaFree(player: Player) {
        AscendDuels.instance.arenaHandler.grid.free()
        player.sendMessage("${ChatColor.GREEN}Arena grid has been freed.")
    }

    @Subcommand("createSchematic")
    @Syntax("<schematic>")
    @Description("Create and load arena schematic")
    fun arenaCreateSchematic(player: Player, schematicName: String) {
        val arenaHandler = AscendDuels.instance.arenaHandler

        if (arenaHandler.getSchematic(schematicName) != null) {
            player.sendMessage("${ChatColor.RED}Schematic $schematicName already exists")
            return
        }

        val schematic = ArenaSchematic(schematicName)
        val schemFile: File = schematic.getSchematicFile()

        if (!schemFile.exists()) {
            player.sendMessage("${ChatColor.RED}No file for $schematicName found. (${schemFile.path})")
            return
        }

        arenaHandler.registerSchematic(schematic)

        try {
            schematic.pasteModelArena()
            arenaHandler.saveSchematics()
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

        player.sendMessage("${ChatColor.GREEN}Schematic created.")
    }

    @Subcommand("listArenas")
    @Syntax("<schematic>")
    @Description("List arenas of a schematic")
    fun arenaListArenas(player: Player, schematicName: String) {
        val arenaHandler = AscendDuels.instance.arenaHandler
        val schematic = arenaHandler.getSchematic(schematicName)

        if (schematic == null) {
            player.sendMessage("${ChatColor.RED}Schematic $schematicName not found.")
            player.sendMessage("${ChatColor.RED}List all schematics with /arena listSchematics")
            return
        }

        player.sendMessage("${ChatColor.RED}------ ${ChatColor.WHITE}${schematic.name} Arenas ${ChatColor.RED}------")

        for (arena: Arena in arenaHandler.getArenas(schematic)) {
            val locationStr = LocationUtils.locToStr(arena.getSpectatorSpawnLocation())
            val occupiedStr =
                if (arena.inUse) "${ChatColor.RED}In Use" else "${ChatColor.GREEN}Open"

            player.sendMessage("${arena.copy}: ${ChatColor.GREEN}$locationStr ${ChatColor.GRAY}- $occupiedStr")
        }
    }

    @Subcommand("repasteSchematic")
    @Syntax("<schematic>")
    @Description("Repaste schematic arenas")
    fun arenaRepasteSchematic(player: Player, schematicName: String) {
        val arenaHandler = AscendDuels.instance.arenaHandler
        val schematic = arenaHandler.getSchematic(schematicName)

        if (schematic == null) {
            player.sendMessage("${ChatColor.RED}Schematic $schematicName not found.")
            player.sendMessage("${ChatColor.RED}List all schematics with /arena listSchematics")
            return
        }

        val currentCopies = arenaHandler.countArenas(schematic)
        if (currentCopies == 0) {
            player.sendMessage("${ChatColor.RED}No copies of ${schematic.name} exist.")
            return
        }

        val grid: ArenaGrid = arenaHandler.grid
        player.sendMessage("${ChatColor.GREEN}Starting...")

        grid.scaleCopies(schematic, 0) {
            player.sendMessage("${ChatColor.GREEN}Removed old maps, creating new copies...")

            grid.scaleCopies(schematic, currentCopies) {
                player.sendMessage("${ChatColor.GREEN}Repasted $currentCopies arenas using the newest ${schematic.name} schematic.")
            }
        }
    }

    @Subcommand("scale")
    @Syntax("<schematic> <count>")
    @Description("Scale a schematic to specific number of arenas")
    fun arenaScale(player: Player, schematicName: String, count: Int) {
        val arenaHandler = AscendDuels.instance.arenaHandler
        val schematic = arenaHandler.getSchematic(schematicName)

        if (schematic == null) {
            player.sendMessage("${ChatColor.RED}Schematic $schematicName not found.")
            player.sendMessage("${ChatColor.RED}List all schematics with /arena listSchematics")
            return
        }

        player.sendMessage("${ChatColor.GREEN}Starting...")

        arenaHandler.grid.scaleCopies(schematic, count) {
            player.sendMessage("${ChatColor.GREEN}Scaled ${schematic.name} to $count copies.")
        }
    }

    @Subcommand("rescaleall")
    @Description("Rescale all schematics")
    fun arenaRescaleAll(player: Player) {
        val handler = AscendDuels.instance.arenaHandler

        handler.schematics.values.forEach { schematic ->
            val totalCopies = handler.getArenas(schematic).size
            arenaScale(player, schematic.name!!, 0)
            arenaScale(player, schematic.name!!, totalCopies)
            player.sendMessage("${ChatColor.GREEN}Rescaling ${schematic.name}...")
        }
    }

    @Subcommand("listSchematics")
    @CommandAlias("listSchems")
    @Description("List all schematics")
    fun arenaListSchems(player: Player) {
        val handler = AscendDuels.instance.arenaHandler
        player.sendMessage(LONG_LINE)
        player.sendMessage(CC.translate("&5&lPotPvP Schematics"))
        player.sendMessage(LONG_LINE)
        handler.schematics.values.forEach { schematic ->
            val size = handler.getArenas(schematic).size
            player.sendMessage(CC.translate("&c${schematic.name} &7| &cArenas using: &f$size"))
        }
        player.sendMessage(LONG_LINE)
    }
}