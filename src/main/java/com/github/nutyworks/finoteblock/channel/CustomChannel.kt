package com.github.nutyworks.finoteblock.channel

class CustomChannel(override val name: String,
                    override val channelType: ChannelType,
                    override val permanent: Boolean) : BaseChannel() {

}