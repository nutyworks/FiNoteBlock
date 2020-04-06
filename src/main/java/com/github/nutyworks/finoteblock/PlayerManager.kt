package com.github.nutyworks.finoteblock

import com.github.nutyworks.finoteblock.channel.IChannel
import com.github.nutyworks.finoteblock.command.CommandFailException
import com.github.nutyworks.finoteblock.noteblock.file.UserSettings
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*
import kotlin.collections.HashMap

class PlayerManager : Listener {
    val playerChannel = HashMap<UUID, IChannel>()
    val playerSetting = HashMap<UUID, UserSettings>()

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId
        moveChannel(uuid, "default")
        FiNoteBlockPlugin.instance.playerManager.playerSetting[uuid] = UserSettings(uuid)
    }

    fun moveChannel(uuid: UUID, channel: String) {
        val currentChannel = playerChannel[uuid]
        if (currentChannel?.name == channel) throw CommandFailException("Already in $channel channel.")

        val changeChannel = FiNoteBlockPlugin.instance.channelManager.channels[channel]
                ?: throw CommandFailException("Could not find channel $channel")

        currentChannel?.recipient?.remove(uuid)
        changeChannel.recipient.add(uuid)
        playerChannel[uuid] = changeChannel

        Bukkit.getPlayer(uuid)?.sendMessage("§6You are moved to $channel channel.")
    }
}