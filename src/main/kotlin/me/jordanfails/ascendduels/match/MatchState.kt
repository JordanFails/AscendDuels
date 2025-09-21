package me.jordanfails.ascendduels.match

import org.bukkit.ChatColor

enum class MatchState(
    val display: String?,
    val color: ChatColor?
) {
    STARTING("Starting", ChatColor.RED),
    ONGOING("In Progress", ChatColor.GREEN),
    ENDED("Over", ChatColor.RED);

    fun getDisplayName(): String {
        return color.toString() + display
    }
}