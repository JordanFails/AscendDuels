package me.jordanfails.ascendduels.arena.listener

import me.jordanfails.ascendduels.arena.event.ArenaReleasedEvent
import me.jordanfails.ascendduels.utils.Cuboid
import org.bukkit.Chunk
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.function.Consumer

/**
 * Remove dropped items when [net.frozenorb.potpvp.arena.Arena]s are released.
 */
class ArenaItemResetListener : Listener {
    @EventHandler
    fun onArenaReleased(event: ArenaReleasedEvent) {
        val bounds: Cuboid? = event.arena.bounds

        // force load all chunks (can't iterate entities in an unload chunk)
        // that are at all covered by this map.
        bounds!!.getChunks().forEach(Consumer { chunk: Chunk? ->
            chunk!!.load()
            for (entity in chunk.entities) {
                // if we remove all entities we might call .remove()
                // on a player (breaks a lot of things)
                if (entity is Item && bounds.contains(entity)) {
                    entity.remove()
                }
            }
        })
    }
}