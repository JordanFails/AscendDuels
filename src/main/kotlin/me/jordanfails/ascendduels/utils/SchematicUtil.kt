package me.jordanfails.ascendduels.utils

import com.sk89q.worldedit.*
import com.sk89q.worldedit.bukkit.BukkitUtil
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.data.DataException
import com.sk89q.worldedit.schematic.SchematicFormat
import com.sk89q.worldedit.session.ClipboardHolder
import me.jordanfails.ascendduels.AscendDuels
import net.pvpwars.core.util.CC
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException

@Suppress("deprecation") // WE 6.x API is deprecated in modern context
object SchematicUtil {

    private val plugin: JavaPlugin = AscendDuels.instance

    /**
     * Save the player's current clipboard selection as a `.schematic` file using WE 6.2
     */
    @Throws(Throwable::class)
    fun save(player: Player, schematicName: String): Location {
        val schematicFile = File(plugin.dataFolder, "schematics/$schematicName.schematic")
        schematicFile.parentFile.mkdirs()

        val wep = Bukkit.getServer().pluginManager.getPlugin("WorldEdit") as WorldEditPlugin
        val we = wep.worldEdit

        val localPlayer = wep.wrapPlayer(player)
        val localSession = we.getSession(localPlayer)
        val clipboardHolder: ClipboardHolder = localSession.clipboard
        val editSession = localSession.createEditSession(localPlayer)

        val min: Vector = clipboardHolder.clipboard.minimumPoint
        val max: Vector = clipboardHolder.clipboard.maximumPoint

        editSession.enableQueue()

        val clipboard = CuboidClipboard(max.subtract(min).add(Vector(1, 1, 1)), min)
        clipboard.copy(editSession)

        SchematicFormat.MCEDIT.save(clipboard, schematicFile)
        editSession.flushQueue()

        player.sendMessage(CC.translate("&aSchematic saved as &f${schematicFile.name}"))


        return BukkitUtil.toLocation(player.world, min)
    }

    /**
     * Paste the `.schematic` file at the given Bukkit location.
     * @return true if successful, false otherwise
     */
    fun paste(schematicName: String, loc: Location): Boolean {
        try {
            val file = File(plugin.dataFolder, "schematics/$schematicName.schematic")
            if (!file.exists()) {
                plugin.logger.warning("Schematic not found: $schematicName at ${file.absolutePath}")
                loc.world?.players?.forEach { it.sendMessage("§cSchematic not found: $schematicName") }
                return false
            }

            if (loc.world == null) {
                plugin.logger.warning("Cannot paste schematic: world is null")
                return false
            }

            val editSession = EditSession(BukkitWorld(loc.world), 1000)
            editSession.enableQueue()

            val format: SchematicFormat = SchematicFormat.getFormat(file)
            val clipboard: CuboidClipboard = format.load(file)

            clipboard.paste(editSession, BukkitUtil.toVector(loc), true)
            editSession.flushQueue()
            
            plugin.logger.info("Successfully pasted schematic $schematicName at ${loc.x}, ${loc.y}, ${loc.z}")
            return true

        } catch (ex: DataException) {
            plugin.logger.severe("DataException while pasting schematic $schematicName: ${ex.message}")
            ex.printStackTrace()
        } catch (ex: IOException) {
            plugin.logger.severe("IOException while pasting schematic $schematicName: ${ex.message}")
            ex.printStackTrace()
        } catch (ex: MaxChangedBlocksException) {
            plugin.logger.severe("MaxChangedBlocksException while pasting schematic $schematicName: ${ex.message}")
            ex.printStackTrace()
        } catch (ex: Exception) {
            plugin.logger.severe("Unexpected error while pasting schematic $schematicName: ${ex.message}")
            ex.printStackTrace()
        }
        return false
    }
}