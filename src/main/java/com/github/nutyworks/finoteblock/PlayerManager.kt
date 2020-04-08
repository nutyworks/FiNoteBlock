package com.github.nutyworks.finoteblock

import com.github.nutyworks.finoteblock.channel.IChannel
import com.github.nutyworks.finoteblock.command.CommandFailException
import com.github.nutyworks.finoteblock.noteblock.file.UserSettings
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerResourcePackStatusEvent
import java.util.*
import kotlin.collections.HashMap

class PlayerManager : Listener {
    val playerChannel = HashMap<UUID, IChannel>()
    val playerSetting = HashMap<UUID, UserSettings>()

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId

        playerChannel[uuid] ?: moveChannel(uuid, "default", true)
        if (playerSetting[uuid] == null)
            FiNoteBlockPlugin.instance.playerManager.playerSetting[uuid] = UserSettings(uuid)

        event.player.setResourcePack("https://github.com/HielkeMinecraft/OpenNoteBlockStudio/blob/master/datafiles/Data/extranotes.zip?raw=true",
                "609d2ea415906ecba8171ade91bda1c1056244ab")
    }

    @EventHandler
    private fun onResourcePackStats(event: PlayerResourcePackStatusEvent) {
        if (event.status == PlayerResourcePackStatusEvent.Status.DECLINED) event.player.sendMessage("§cSome notes will not be played without the resource pack.")
        else if (event.status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) event.player.sendMessage("§cFailed to download the resource pack. Download the resource pack at §8 https://github.com/HielkeMinecraft/OpenNoteBlockStudio/blob/master/datafiles/Data/extranotes.zip?raw=true")
    }

    fun moveChannel(uuid: UUID, channel: String, force: Boolean = false) {
        val currentChannel = playerChannel[uuid]
        if (!force && currentChannel?.name == channel)
                throw CommandFailException("Already in $channel channel.")

        val changeChannel = FiNoteBlockPlugin.instance.channelManager.channels[channel]
                ?: throw CommandFailException("Could not find channel $channel")

        currentChannel?.recipient?.remove(uuid)
        changeChannel.recipient.add(uuid)
        playerChannel[uuid] = changeChannel

        if (!force) {
            Bukkit.getPlayer(uuid)?.sendMessage("§6You are moved to §a$channel §6channel.")
        }
    }
}