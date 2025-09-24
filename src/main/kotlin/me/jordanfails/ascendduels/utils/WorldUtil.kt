package me.jordanfails.ascendduels.utils

import me.jordanfails.ascendduels.AscendDuels
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.generator.ChunkGenerator
import java.util.*

object WorldUtil {
    
    private const val DUELS_WORLD_NAME = "duels"
    
    /**
     * Ensures the duels world exists, creating it if necessary
     * @return The duels world instance
     */
    fun ensureDuelsWorld(): World {
        var world = Bukkit.getWorld(DUELS_WORLD_NAME)
        
        if (world == null) {
            AscendDuels.instance.logger.info("Duels world not found, creating new world...")
            world = createDuelsWorld()
            AscendDuels.instance.logger.info("Duels world created successfully!")
        } else {
            AscendDuels.instance.logger.info("Duels world already exists, ensuring spawn point is set correctly.")
            ensureSpawnPoint(world)
        }
        
        return world
    }
    
    /**
     * Ensures the spawn point is set to (0, 100, 0) with bedrock
     * @param world The world to set the spawn point for
     */
    private fun ensureSpawnPoint(world: World) {
        val spawnLocation = Location(world, 0.0, 100.0, 0.0)
        
        // Set spawn location
        world.setSpawnLocation(spawnLocation.x.toInt(), spawnLocation.y.toInt(), spawnLocation.z.toInt())
        
        // Place bedrock at spawn if not already there
        val block = world.getBlockAt(spawnLocation)
        if (block.type != Material.BEDROCK) {
            block.type = Material.BEDROCK
            AscendDuels.instance.logger.info("Placed bedrock at spawn point (0, 100, 0)")
        }
    }
    
    /**
     * Creates a new duels world with empty chunks
     * @return The created world instance
     */
    private fun createDuelsWorld(): World {
        val worldCreator = WorldCreator(DUELS_WORLD_NAME)
            .type(WorldType.FLAT)
            .generator(EmptyChunkGenerator())
            .generateStructures(false)
        
        val world = worldCreator.createWorld() ?: throw IllegalStateException("Failed to create duels world")
        
        // Set spawn point to (0, 100, 0) and place bedrock there
        val spawnLocation = Location(world, 0.0, 100.0, 0.0)
        world.setSpawnLocation(0, 100, 0)
        world.getBlockAt(spawnLocation).type = Material.BEDROCK
        
        AscendDuels.instance.logger.info("Set world spawn to (0, 100, 0) and placed bedrock")
        
        return world
    }
    
    /**
     * Custom chunk generator that creates empty chunks with bedrock at spawn
     */
    private class EmptyChunkGenerator : ChunkGenerator() {
        override fun generateChunkData(world: World, random: Random, x: Int, z: Int, biome: BiomeGrid): ChunkData {
            val chunkData = createChunkData(world)
            
            // Place bedrock at (0, 100, 0) if this is the spawn chunk (0, 0)
            if (x == 0 && z == 0) {
                chunkData.setBlock(0, 100, 0, Material.BEDROCK)
            }
            
            return chunkData
        }
    }
}
