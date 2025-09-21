package me.jordanfails.ascendduels.api.serializable

import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration

interface YamlSerializable : Serializable<YamlConfiguration?> {
    public override fun serializeToString(): String? {
        return serialize()?.saveToString()
    }

    fun deserialize(string: String?) {
        val configuration = YamlConfiguration()
        try {
            configuration.loadFromString(string)
        } catch (ex: InvalidConfigurationException) {
            ex.printStackTrace()
        }

        deserialize(configuration)
    }
}