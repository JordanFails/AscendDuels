package me.jordanfails.ascendduels.api.service

import me.jordanfails.ascendduels.AscendDuels

interface Service {
    fun load()
    fun unload()

    companion object {
        val duels: AscendDuels = AscendDuels.instance
    }
}