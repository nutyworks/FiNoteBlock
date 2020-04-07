package com.github.nutyworks.finoteblock.channel

import com.github.nutyworks.finoteblock.FiNoteBlockPlugin
import com.github.nutyworks.finoteblock.command.CommandFailException
import java.util.*
import kotlin.collections.HashMap

class ChannelManager {
    val channels = HashMap<String, IChannel>()

    init {
        channels["default"] = DefaultChannel("default")
    }

    fun addChannel(name: String, type: ChannelType, permanent: Boolean = false) {
        if (channels.containsKey(name)) throw CommandFailException("The channel name $name is taken.")
        channels[name] = CustomChannel(name, type, permanent)
    }

    fun removeChannel(name: String) {
        val channel = channels[name] ?: throw CommandFailException("Could not find a channel $name.")

        if (channel is DefaultChannel) throw CommandFailException("Cannot delete the default channel.")

        (channel.recipient.clone() as ArrayList<*>).forEach {
            FiNoteBlockPlugin.instance.playerManager.moveChannel(it as UUID, "default")
        }

        channel.stop()
        channels.remove(name)
    }
}