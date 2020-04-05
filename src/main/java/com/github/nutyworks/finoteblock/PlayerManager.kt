package com.github.nutyworks.finoteblock

import com.github.nutyworks.finoteblock.channel.IChannel
import com.github.nutyworks.finoteblock.util.FailMessage
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*
import kotlin.collections.HashMap

class PlayerManager : Listener {
    val playerChannel = HashMap<UUID, IChannel>()

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        moveChannel(event.player.uniqueId, "default")
    }

    fun moveChannel(uuid: UUID, channel: String): FailMessage? {
        val currentChannel = playerChannel[uuid]
        if (currentChannel?.name == channel) return FailMessage("Already in $channel channel.")

        val changeChannel = FiNoteBlockPlugin.instance.channelManager.channels[channel]
                ?: return FailMessage("Could not find channel $channel")

        currentChannel?.recipient?.remove(uuid)
        changeChannel.recipient.add(uuid)
        playerChannel[uuid] = changeChannel

        Bukkit.getPlayer(uuid)?.sendMessage("§6You are moved to $channel channel.")

        return null
    }
}