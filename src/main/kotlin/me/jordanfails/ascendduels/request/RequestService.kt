package me.jordanfails.ascendduels.request

import me.jordanfails.ascendduels.api.service.Service
import me.jordanfails.ascendduels.arena.ArenaSchematic
import me.jordanfails.ascendduels.kit.Kit
import net.pvpwars.core.util.runnable.RunnableBuilder
import java.util.*

class RequestService : Service {
    private val byRequestID: MutableMap<UUID?, Request?> = HashMap<UUID?, Request?>()
    private val bySenderID: MutableMap<UUID?, MutableList<Request>?> = HashMap<UUID?, MutableList<Request>?>()

    fun hasExistingRequest(sender: UUID?, receiver: UUID?): Boolean {
        val requestList: MutableList<Request>? = bySenderID[sender]
        return if (requestList != null && !requestList.isEmpty()) {
            requestList.stream()
                .filter { request: Request -> request.receiver!! == receiver }
                .findFirst()
                .orElse(null) != null
        } else {
            false
        }
    }

    fun createRequest(sender: UUID?, receiver: UUID?, kit: Kit?, arena: ArenaSchematic?): Request {
        val request = Request(UUID.randomUUID(), sender, receiver, kit, arena)

        byRequestID[request.id] = request
        bySenderID.computeIfAbsent(sender) { _: UUID? -> ArrayList<Request>() }!!.add(request)

        RunnableBuilder.bind { removeRequest(request.id) }.runSyncLater(20L * 30L)

        return request
    }

    fun removeRequest(requestID: UUID?): Request? {
        val request: Request? = byRequestID.remove(requestID)
        if (request != null) {
            val requestList: MutableList<Request>? = bySenderID[request.sender]
            requestList?.removeIf { request1: Request -> request1.id == requestID }
        }
        return request
    }

    fun removeAllRequests(sender: UUID?) {
        val requestList: MutableList<Request>? = bySenderID.remove(sender)
        if (requestList != null) {
            for (request in requestList) {
                byRequestID.remove(request.id)
            }
        }
    }

    public override fun load() {
    }

    public override fun unload() {
    }
}