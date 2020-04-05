package com.github.nutyworks.finoteblock.channel

import com.github.nutyworks.finoteblock.util.FailMessage

class ChannelManager {
    val channels = HashMap<String, IChannel>()

    init {
        channels["default"] = DefaultChannel("default")
    }

    fun addChannel(name: String, type: ChannelType, permanent: Boolean = false): FailMessage? {
        if (channels.containsKey(name)) return FailMessage("The channel name $name is taken.")
        channels[name] = CustomChannel(name, type, permanent)

        return null
    }

    fun removeChannel(name: String): FailMessage? {
        val channel = channels[name] ?: return FailMessage("Could not find a channel $name.")

        if (channel is DefaultChannel) return FailMessage("Cannot delete the default channel.")

        return null
    }
}