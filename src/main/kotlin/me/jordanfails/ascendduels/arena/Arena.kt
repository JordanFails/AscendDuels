package me.jordanfails.ascendduels.arena

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import me.jordanfails.ascendduels.api.serializable.JsonSerializable
import me.jordanfails.ascendduels.api.serializable.builder.JsonObjectBuilder
import net.pvpwars.core.Core
import net.pvpwars.core.util.location.LocUtil
import me.jordanfails.ascendduels.utils.SchematicUtil
import net.pvpwars.duels.util.SerializationUtil
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.inventory.ItemStack

class Arena() : JsonSerializable {

    var name: String? = null
    var displayName: String? = null
    var displayItem: ItemStack? = null
    var schematicName: String? = null
    var schematicLoc: Location? = null
    var tags: MutableSet<ArenaTag> = HashSet()
    var spawnPoints: MutableList<Location> = ArrayList()
    var genArenas: MutableList<GenArena> = ArrayList()

    constructor(name: String) : this() {
        this.name = name
        this.tags = HashSet()
        this.spawnPoints = ArrayList()
        this.genArenas = ArrayList()
    }

    val isComplete: Boolean
        get() = name != null &&
                displayName != null &&
                schematicName != null &&
                displayItem != null &&
                schematicLoc != null &&
                tags.isNotEmpty() &&
                spawnPoints.isNotEmpty()

    fun hasTag(arenaTag: ArenaTag): Boolean = tags.contains(arenaTag)

    fun getGenerateTasks(world: World, generateIndex: Int): List<Runnable> {
        val tasks = mutableListOf<Runnable>()
        val distance = 300
        val x = generateIndex * distance

        for (z in 0 until distance * 2 step distance) {
            tasks.add(Runnable {
                val pasteLoc = Location(world, x.toDouble(), 100.0, z.toDouble())
                schematicName?.let {
                    SchematicUtil.paste(it, pasteLoc)
                }
                genArenas.add(GenArena(pasteLoc, false))
            })
        }
        return tasks
    }

    fun unoccupiedGenArena(): GenArena? =
        genArenas.firstOrNull { !it.occupied }

    fun getSpawnLocation(arena: GenArena, spawnPointIndex: Int): Location {
        val spawnPoint = spawnPoints[spawnPointIndex]
        return arena.location.clone().add(spawnPoint.toVector()).apply {
            yaw = spawnPoint.yaw
            pitch = spawnPoint.pitch
        }
    }

    override fun serialize(): JsonObject {
        val gson: Gson = Core.getInstance().gson
        return JsonObjectBuilder()
            .addProperty("name", name)
            .addProperty("displayName", displayName)
            .addProperty("displayItem", SerializationUtil.itemStackToBase64(displayItem))
            .addProperty("schematicName", schematicName)
            .addProperty("schematicLoc", LocUtil.serializeLocation(schematicLoc))
            .addProperty("tags", gson.toJsonTree(tags))
            .addProperty(
                "spawnPoints",
                gson.toJsonTree(spawnPoints.map { LocUtil.serializeLocation(it) })
            )
            .addProperty(
                "genArenas",
                gson.toJsonTree(genArenas.map { it.serialize() })
            )
            .jsonObject
    }

    override fun deserialize(jsonObject: JsonObject?) {
        val gson: Gson = Core.getInstance().gson

        if (jsonObject == null) return

        name = jsonObject["name"].asString
        displayName = jsonObject["displayName"].asString

        val itemStr = jsonObject["displayItem"].asString
        if (itemStr.isNotEmpty()) {
            displayItem = SerializationUtil.itemStackFromBase64(itemStr)
        }

        schematicName = jsonObject["schematicName"].asString
        schematicLoc = LocUtil.deserializeLocation(jsonObject["schematicLoc"].asString)

        tags = gson.fromJson(
            jsonObject["tags"].asJsonArray,
            object : TypeToken<Set<ArenaTag>>() {}.type
        )

        val points: List<String> = gson.fromJson(
            jsonObject["spawnPoints"].asJsonArray,
            object : TypeToken<List<String>>() {}.type
        )
        spawnPoints = points.map { LocUtil.deserializeLocation(it) }.toMutableList()

        genArenas = try {
            val generated: List<JsonObject> = gson.fromJson(
                jsonObject["genArenas"].asJsonArray,
                object : TypeToken<List<JsonObject>>() {}.type
            )
            generated.map { GenArena(it) }.toMutableList()
        } catch (_: NullPointerException) {
            ArrayList()
        }
    }
}