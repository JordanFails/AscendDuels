package me.jordanfails.ascendduels.inventory

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.jordanfails.ascendduels.api.service.Service
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.TimeUnit

class RiskPostMatchInventoryService : Service {

    private val cache =
        CacheBuilder.newBuilder()
            .expireAfterWrite(3L, TimeUnit.MINUTES)
            .build<UUID, RiskPostMatchInventory>()

    fun create(player: Player): RiskPostMatchInventory {
        val inventory = RiskPostMatchInventory(player)
        cache.put(inventory.uuid, inventory)
        return inventory
    }

    fun get(uuid: UUID): RiskPostMatchInventory? {
        return cache.getIfPresent(uuid)
    }

    override fun load() {
        // Nothing to load on startup
    }

    override fun unload() {
        // Nothing to unload; cache auto-expires
        cache.invalidateAll()
    }
}