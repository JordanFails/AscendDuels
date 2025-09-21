package me.jordanfails.ascendduels.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.arena.Arena
import me.jordanfails.ascendduels.arena.ArenaService
import me.jordanfails.ascendduels.arena.ArenaTag
import me.jordanfails.ascendduels.utils.SchematicUtil
import net.pvpwars.core.util.isAir
import net.pvpwars.core.util.item.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.ConcurrentLinkedQueue

@CommandAlias("arena")
@CommandPermission("command.AscendDuels.admin")
class ArenaCommand : BaseCommand() {

    private val arenaService: ArenaService = AscendDuels.instance.arenaService

    @Default
    fun onCommand(sender: CommandSender) {
        sender.sendMessage("/arena <subcommand>")
    }

    @Subcommand("list")
    fun onList(sender: CommandSender) {
        sender.sendMessage(AscendDuels.prefix("&fArena List:"))
        for (arena in arenaService.all()) {
            sender.sendMessage("")
            sender.sendMessage("name: ${arena.name}")
            sender.sendMessage("displayName: ${arena.displayName}")
            sender.sendMessage("schematic: ${arena.schematicName}")
            sender.sendMessage(
                "tags: " + arena.tags.joinToString(", ") { it.name }
            )
        }
    }

    @Subcommand("create")
    fun onCreate(sender: CommandSender, @Name("name") name: String) {
        var arena = arenaService.get(name)
        if (arena == null) {
            arena = Arena(name)
            arenaService.add(arena)
            arenaService.saveAll()
            sender.sendMessage(AscendDuels.prefix("&aSuccessfully created the arena: $name"))

            if (!arena.isComplete) {
                sender.sendMessage(AscendDuels.prefix("&6The arena wasn't saved as it's not completed yet!"))
                sender.sendMessage(AscendDuels.prefix("&6Next Recommended: &f/arena displayName $name <displayName>"))
            } else {
                sender.sendMessage(AscendDuels.prefix("&aThe arena was saved!"))
            }
        } else {
            sender.sendMessage(AscendDuels.prefix("&cAn arena with this name already exists!"))
        }
    }

    @Subcommand("delete")
    @CommandCompletion("@arenas")
    fun onDelete(sender: CommandSender, @Name("arena") arena: Arena) {
        arenaService.remove(arena.name!!)
        arenaService.saveAll()
        sender.sendMessage(AscendDuels.prefix("&aSuccessfully deleted the arena: ${arena.name}"))
    }

    @Subcommand("displayName")
    @CommandCompletion("@arenas")
    fun onDisplayName(sender: CommandSender, @Name("arena") arena: Arena, @Name("name") displayName: String) {
        arena.displayName = displayName
        arena.displayItem?.let {
            arena.displayItem = ItemBuilder(it).name(arena.displayName!!)
        }
        arenaService.saveAll()

        sender.sendMessage(AscendDuels.prefix("&aUpdated display name for arena: ${arena.name}"))
        if (!arena.isComplete) {
            sender.sendMessage(AscendDuels.prefix("&6The arena wasn't saved as it's not completed yet!"))
            sender.sendMessage(
                AscendDuels.prefix("&6Next Recommended: &f/arena displayItem ${arena.name}")
            )
        } else {
            sender.sendMessage(AscendDuels.prefix("&aThe arena was saved!"))
        }
    }

    @Subcommand("displayItem")
    @CommandCompletion("@arenas")
    fun onDisplayItem(@Flags("itemheld") player: Player, arena: Arena) {
        arena.displayItem = player.inventory.itemInHand
        if(player.inventory.itemInHand == null || player.inventory.itemInHand.type.isAir()) {
            player.sendMessage(AscendDuels.prefix("&cYou must be holding an item to set as the display item!"))
            return
        }
        arena.displayName?.let {
            arena.displayItem = ItemBuilder(arena.displayItem!!).name(it)
        }
        arenaService.saveAll()

        player.sendMessage(AscendDuels.prefix("&aUpdated display item for arena: ${arena.name}"))
        if (!arena.isComplete) {
            player.sendMessage(AscendDuels.prefix("&6The arena wasn't saved as it's not completed yet!"))
            player.sendMessage(
                AscendDuels.prefix("&6Next Recommended: &f/arena tag ${arena.name} <arenaTag>")
            )
        } else {
            player.sendMessage(AscendDuels.prefix("&aThe arena was saved!"))
        }
    }

    @Subcommand("tag")
    @CommandCompletion("@arenas @arenaTags")
    fun onTag(sender: CommandSender, arena: Arena, arenaTag: ArenaTag) {
        if (!arena.tags.add(arenaTag)) {
            arena.tags.remove(arenaTag)
        }
        arenaService.saveAll()

        sender.sendMessage(AscendDuels.prefix("&aUpdated tags for arena: ${arena.name}"))
        sender.sendMessage(
            AscendDuels.prefix("&aNew tags: " + arena.tags.joinToString(", ") { it.name })
        )

        if (!arena.isComplete) {
            sender.sendMessage(AscendDuels.prefix("&6The arena wasn't saved as it's not completed yet!"))
            sender.sendMessage(
                AscendDuels.prefix("&6Next Recommended: &f/arena schematic ${arena.name} <schemName>")
            )
        } else {
            sender.sendMessage(AscendDuels.prefix("&aThe arena was saved!"))
        }
    }

    @Subcommand("schematic")
    @CommandCompletion("@arenas")
    fun onSchematic(player: Player, arena: Arena, schemName: String) {
        val schemLocation: Location
        try {
            schemLocation = SchematicUtil.save(player, schemName)
        } catch (t: Throwable) {
            t.printStackTrace()
            player.sendMessage(AscendDuels.prefix("&cAn error occurred while saving schematic!"))
            return
        }

        arena.schematicName = schemName
        arena.schematicLoc = schemLocation
        arenaService.saveAll()

        player.sendMessage(AscendDuels.prefix("&aUpdated schematic for arena: ${arena.name}"))
        if (!arena.isComplete) {
            player.sendMessage(AscendDuels.prefix("&6The arena wasn't saved as it's not completed yet!"))
            player.sendMessage(
                AscendDuels.prefix("&6Next Recommended: &f/arena spawnPoint ${arena.name} <number>")
            )
        } else {
            player.sendMessage(AscendDuels.prefix("&aThe arena was saved!"))
        }
    }

    @Subcommand("spawnPoint")
    @CommandCompletion("@arenas")
    fun onSpawnPoint(player: Player, arena: Arena, number: Int) {
        if (arena.schematicLoc == null) {
            player.sendMessage(AscendDuels.prefix("&cThe schematic must be set first!"))
            return
        }

        val schemLoc = arena.schematicLoc!!
        val relativeLoc = player.location.clone().subtract(schemLoc)

        val spawnPoints = arena.spawnPoints
        if (spawnPoints.size > number) {
            player.sendMessage(AscendDuels.prefix("&cThe maximum number you can put is: ${spawnPoints.size}"))
            return
        }

        spawnPoints.add(number - 1, relativeLoc)
        arenaService.saveAll()

        player.sendMessage(AscendDuels.prefix("&aUpdated spawn point for arena: ${arena.name}"))
        if (!arena.isComplete) {
            player.sendMessage(AscendDuels.prefix("&6The arena wasn't saved as it's not completed yet!"))
            player.sendMessage(
                AscendDuels.prefix("&6Next Recommended: &f/arena displayName ${arena.name} <displayName>")
            )
        } else {
            player.sendMessage(AscendDuels.prefix("&aThe arena was saved!"))
        }
    }

    @Subcommand("testPaste")
    @CommandCompletion("@arenas")
    fun onTestPaste(player: Player, arena: Arena) {
        val loaded = arenaService.get(arena.name!!) ?: return
        SchematicUtil.paste(loaded.schematicName!!, player.location)
    }

    @Subcommand("tpSpawnPoint")
    @CommandCompletion("@arenas @spawnPointTypes")
    fun onTpSpawnPoint(player: Player, arena: Arena, number: Int) {
        val loaded = arenaService.get(arena.name!!) ?: return
        val relativeSpawnPoint = loaded.spawnPoints[number - 1]
        val schemLoc = loaded.schematicLoc!!
        val spawnPoint = schemLoc.clone().add(relativeSpawnPoint)
        spawnPoint.yaw = relativeSpawnPoint.yaw
        spawnPoint.pitch = relativeSpawnPoint.pitch

        player.teleport(spawnPoint)
    }

    @Subcommand("generateArenas")
    @CommandCompletion("@worlds")
    fun onGenerateArenas(sender: CommandSender, worldName: String) {
        val world: World = Bukkit.getWorld(worldName)
            ?: return sender.sendMessage(AscendDuels.prefix("&cWorld not found: $worldName"))

        val arenas = arenaService.all()
        sender.sendMessage(AscendDuels.prefix("Generating arenas"))

        val taskQueue = ConcurrentLinkedQueue<Runnable>()
        arenas.forEachIndexed { index, arena ->
            arena.genArenas.clear()
            taskQueue.addAll(arena.getGenerateTasks(world, index))
        }

        object : BukkitRunnable() {
            override fun run() {
                val task = taskQueue.poll()
                if (task != null) {
                    task.run()
                    sender.sendMessage("Completed generate task (${taskQueue.size} left)")
                } else {
                    cancel()
                    arenaService.saveAll()
                    sender.sendMessage(AscendDuels.prefix("&aGeneration complete!"))
                }
            }
        }.runTaskTimer(AscendDuels.instance, 20L, 20L)
    }
}