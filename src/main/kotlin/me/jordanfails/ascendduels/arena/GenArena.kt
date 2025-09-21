package me.jordanfails.ascendduels.arena

import com.google.gson.JsonObject
import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.api.serializable.JsonSerializable
import me.jordanfails.ascendduels.api.serializable.builder.JsonObjectBuilder
import net.pvpwars.core.util.location.LocUtil
import org.bukkit.Location

data class GenArena(
    var location: Location,
    var occupied: Boolean = false
) : JsonSerializable {

    constructor(jsonObject: JsonObject) : this(
        location = LocUtil.deserializeLocation(jsonObject.get("location").asString),
        occupied = jsonObject.get("occupied").asBoolean
    ) {
        // ensure the world is registered as soon as we restore from JSON
        AscendDuels.instance.arenaService!!.registerWorld(location.world)
    }

    override fun serialize(): JsonObject {
        return JsonObjectBuilder()
            .addProperty("location", LocUtil.serializeLocation(location))
            .addProperty("occupied", occupied)
            .jsonObject
    }

    override fun deserialize(jsonObject: JsonObject?) {
        location = LocUtil.deserializeLocation(jsonObject!!.get("location").asString)
        occupied = jsonObject!!.get("occupied").asBoolean

        AscendDuels.instance
            .arenaService
            .registerWorld(location.world)
    }
}