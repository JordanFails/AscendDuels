package me.jordanfails.ascendduels.inventory

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import me.jordanfails.ascendduels.api.service.Service
import me.jordanfails.ascendduels.inventory.MatchInventory
import me.jordanfails.ascendduels.match.MatchStatistics
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit

class MatchInventoryService : Service {
    private val cache: Cache<UUID?, MatchInventory?> = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build<UUID?, MatchInventory?>()

    fun cacheInventory(player: Player, statistics: MatchStatistics) {
        cache.put(player.uniqueId, newInventory(player, statistics))
    }

    fun get(player: Player): MatchInventory? {
        return cache.getIfPresent(player.uniqueId)
    }

    fun get(uuid: UUID): MatchInventory? {
        return cache.getIfPresent(uuid)
    }

    private fun newInventory(player: Player, statistics: MatchStatistics): MatchInventory {
        return MatchInventory(player, statistics)
    }

    override fun load() {
    }

    override fun unload() {
    }
}