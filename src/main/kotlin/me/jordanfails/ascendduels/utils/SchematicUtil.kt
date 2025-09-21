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
     */
    fun paste(schematicName: String, loc: Location) {
        try {
            val file = File(plugin.dataFolder, "schematics/$schematicName.schematic")
            if (!file.exists()) {
                loc.world?.players?.forEach { it.sendMessage("§cSchematic not found: $schematicName") }
                return
            }

            val editSession = EditSession(BukkitWorld(loc.world), 1000)
            editSession.enableQueue()

            val format: SchematicFormat = SchematicFormat.getFormat(file)
            val clipboard: CuboidClipboard = format.load(file)

            clipboard.paste(editSession, BukkitUtil.toVector(loc), true)
            editSession.flushQueue()

        } catch (ex: DataException) {
            ex.printStackTrace()
        } catch (ex: IOException) {
            ex.printStackTrace()
        } catch (ex: MaxChangedBlocksException) {
            ex.printStackTrace()
        }
    }
}