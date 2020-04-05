package com.github.nutyworks.finoteblock.util

import org.bukkit.command.CommandSender

class FailMessage(private val message: String) {
    fun sendMessage(format: String, sender: CommandSender) {
        sender.sendMessage(String.format(format, message))
    }
}