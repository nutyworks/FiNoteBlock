package com.github.nutyworks.finoteblock.command

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

abstract class AbstractExecutorCompleter : CommandExecutor, TabCompleter {

    lateinit var sender: CommandSender
    lateinit var command: Command
    lateinit var label: String
    lateinit var alias: String
    lateinit var args: Array<out String>
    var player: Player? = null

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        this.sender = sender
        this.command = command
        this.label = label
        this.args = args

        if (sender is Player)
            this.player = sender

        try {
            command()
        } catch (e: CommandFailException) {
            sender.sendMessage("${ChatColor.RED}${e.message}")
        } catch (e: PlayerOnlyCommandException) {
            sender.sendMessage("${ChatColor.RED}${e.message}")
        } catch (e: Exception) {
            sender.sendMessage("${ChatColor.RED}Error while executing command: ${e.message}")
            e.printStackTrace()
        }

        return true
    }

    protected abstract fun command()

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        this.sender = sender
        this.command = command
        this.alias = alias
        this.args = args

        if (sender is Player)
            player = sender

        return tabComplete()
    }

    protected abstract fun tabComplete(): MutableList<String>
}