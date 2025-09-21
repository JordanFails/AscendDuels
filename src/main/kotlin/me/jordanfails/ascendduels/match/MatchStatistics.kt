package me.jordanfails.ascendduels.match

import org.bukkit.potion.PotionEffect

class MatchStatistics {
    var damageDealt: Int = 0
    var longestCombo: Int = 0
    var currentCombo: Int = 0
    var hitsThrown: Int = 0
    var hitsLanded: Int = 0
    var hitsMissed: Int = 0
    var healsUsed: Int = 0
    var healsMissed: Int = 0
    var healsRemaining: Int = 0
    var health: Float = 0f
    var hunger: Float = 0f
    var effects: MutableList<PotionEffect?>? = null
    var healType: HealType = HealType.NONE

    enum class HealType {
        NONE,
        POT,
        SOUP,
    }
}