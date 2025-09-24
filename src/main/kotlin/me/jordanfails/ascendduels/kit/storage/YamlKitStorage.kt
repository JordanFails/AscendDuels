package me.jordanfails.ascendduels.kit.storage

import me.jordanfails.ascendduels.AscendDuels
import me.jordanfails.ascendduels.kit.KitService
import net.pvpwars.core.util.config.Configuration
import me.jordanfails.ascendduels.kit.Kit

class YamlKitStorage(private val kitService: KitService) : KitStorage {
    private val config: Configuration = object : Configuration("kits", AscendDuels.instance) {}

    override fun loadKits() {
        val section = config.getConfig().getConfigurationSection("kits")
        if (section != null) {
            for (kitName in section.getKeys(true)) {
                val kit = Kit()
                kit.deserialize(section.getString(kitName))

                kitService.add(kit)
            }
        }
    }

    override fun saveKits() {
        kitService.getMap().forEach { (kitName: String?, kit: Kit?) ->
            if (kit?.isComplete == true) config.getConfig().set("kits.$kitName", kit.serializeToPrettyString())
        }
        config.save()
    }
}