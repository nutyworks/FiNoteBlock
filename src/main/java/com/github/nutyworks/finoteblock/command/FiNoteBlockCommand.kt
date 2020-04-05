package com.github.nutyworks.finoteblock.command

import com.github.nutyworks.finoteblock.FiNoteBlockPlugin
import com.github.nutyworks.finoteblock.noteblock.file.FileManager
import com.github.nutyworks.finoteblock.noteblock.file.UserSettings
import com.github.nutyworks.finoteblock.noteblock.song.NoteBlockSong
import com.github.nutyworks.finoteblock.noteblock.song.Recipient
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File

class FiNoteBlockCommand(val plugin: FiNoteBlockPlugin) : ICommandExecutorCompleter {

    companion object {
        val cmdMap = HashMap<String, ICommandExecutorCompleter>().apply {
            put("list", List())
            put("add", Add())
            put("stop", Stop())
            put("pause", Pause())
            put("resume", Resume())
            put("settings", Settings())
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            return false
        }

        return cmdMap[args[0]]?.onCommand(sender, command, label, args) ?: false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        if (args.size == 1)
            return cmdMap.keys.filter { it.startsWith(args[0]) }.toMutableList()

        return cmdMap[args[0]]?.onTabComplete(sender, command, alias, args) ?: mutableListOf()
    }

    class List : ICommandExecutorCompleter {
        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

            if (args.size == 1) {
                playableList(sender, 1)
            } else if (args[1] == "playable") {
                if (args.size >= 3) {
                    if (args[2].matches(Regex("^\\d+$"))) {
                        playableList(sender, args[2].toInt())
                    }
                } else {
                    playableList(sender, 1)
                }
            } else
                if (args[1] == "playing") {
                    if (sender !is Player) {
                        sender.sendMessage("no console")
                        return true
                    }
                    val channel = FiNoteBlockPlugin.instance.playerManager.playerChannel[sender.uniqueId]!!

                    sender.sendMessage("Now playing in ${channel.name}: \n §a${channel.playing?.name ?: "None"}")
                    sender.sendMessage("Queued in ${channel.name}: ")
                    channel.queue.elements().toList().forEachIndexed { index, song ->
                        sender.sendMessage("§6$index. §a${song.name} §7(${song.playId})")
                    }
                } else if (args[1].matches(Regex("^\\d+$"))) {
                    playableList(sender, args[1].toInt())
                } else {
                    sender.sendMessage("/nb list [playing|playable]")
                }

            return true
        }

        fun playableList(sender: CommandSender, page: Int) {
            val files = FileManager.nbsFiles

            if (page == 0) return sender.sendMessage("${ChatColor.RED}No page.")

            val realPage = page - 1

            if (files != null) {
                if (realPage > files.size / 10) sender.sendMessage("${ChatColor.RED}No page.")
                sender.sendMessage("----- NBS song (Page: ${realPage + 1}/${files.size / 10 + 1}) -----")
                for (i in realPage * 10..realPage * 10 + 9) {
                    if (files.size <= i)
                        break
                    sender.sendMessage("${ChatColor.GOLD}$i. " +
                            "${if (files[i].contains(Regex("\\s"))) ChatColor.YELLOW else ChatColor.GREEN}${files[i]}")
                }
            } else {
                sender.sendMessage("${ChatColor.RED}No .nbs file in ./plugins/FiNoteblock/nbs/")
            }
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
            if (args.size == 2) {
                return setOf("playing", "playable").filter { it.startsWith(args[1]) }.toMutableList()
            }

            return mutableListOf()
        }
    }

    class Add : ICommandExecutorCompleter {
        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            if (sender !is Player) {
                sender.sendMessage("Only for players.")
                return true
            }

            val file: File

            if (args[1] == "id") {
                file = File("${FiNoteBlockPlugin.instance.dataFolder}\\nbs\\${FileManager.nbsFiles?.get(args[2].toInt())}.nbs")
            } else if (args[1] == "name") {
                file = File("${FiNoteBlockPlugin.instance.dataFolder}\\nbs\\${args[2]}.nbs")
            } else if (args[1] == "random") {
                file = File("${FiNoteBlockPlugin.instance.dataFolder}\\nbs\\${FileManager.nbsFiles?.random()}.nbs")
            } else {
                sender.sendMessage("/nb add <id|name|random>")
                return true
            }

            if (!file.exists()) {
                sender.sendMessage("File does not exists.")
                return true
            }

            val song = NoteBlockSong(file)
            val channel = FiNoteBlockPlugin.instance.playerManager.playerChannel[sender.uniqueId]!!
            channel.add(song)
            sender.sendMessage("§6Queued §a${song.name}§6 in ${channel.name}. Play ID is ${song.playId}.")
            return true
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {

            if (args.size == 2) {
                return setOf("id", "name", "random").filter { it.startsWith(args[1]) }.toMutableList()
            } else if (args.size == 3 && args[1] == "id") {
                return FileManager.nbsFiles?.indices?.toSet()
                        ?.filter { i -> i.toString().startsWith(args[2]) }
                        ?.map { i -> i.toString() }
                        ?.toMutableList() ?: mutableListOf()
            } else if (args.size >= 3 && args[1] == "name") {
                return FileManager.nbsFiles?.toSet()
                        ?.filter { !it.contains(Regex("\\s")) && it.startsWith(args[2]) }
                        ?.toMutableList() ?: mutableListOf()
            }

            return mutableListOf()
        }
    }

    class Stop : ICommandExecutorCompleter {
        override fun onCommand(sender: CommandSender,
                               command: Command, label: String, args: Array<out String>): Boolean {
            if (sender !is Player) {
                sender.sendMessage("no console")
                return true
            }

            FiNoteBlockPlugin.instance.playerManager.playerChannel[sender.uniqueId]!!.stop()?.sendMessage("§c%s", sender)
                    ?: sender.sendMessage("§6Stopped playing song and removed all queued song.")

            return true
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
            return mutableListOf()
        }
    }

    class Pause : ICommandExecutorCompleter {
        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            if (sender !is Player) {
                sender.sendMessage("no console")
                return true
            }

            FiNoteBlockPlugin.instance.playerManager.playerChannel[sender.uniqueId]!!.pause()?.sendMessage("§c%s", sender)
                    ?: sender.sendMessage("§6Song paused. §f/nb resume §6to resume.")

            return true
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
            return mutableListOf()
        }
    }

    class Resume : ICommandExecutorCompleter {
        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            if (sender !is Player) {
                sender.sendMessage("no console")
                return true
            }

            FiNoteBlockPlugin.instance.playerManager.playerChannel[sender.uniqueId]!!.resume()?.sendMessage("§c%s", sender)
                    ?: sender.sendMessage("§6Song resumed. §f/nb pause §6to pause.")

            return true
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
            return mutableListOf()
        }
    }

    class Settings : ICommandExecutorCompleter {
        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            if (sender !is Player) {
                sender.sendMessage("Only for players.")
                return true
            }

            if (args.size == 1) {
                sender.sendMessage("/nb settings <setting>")
                return true
            }
            when (args[1]) {
                "recipient" -> {
                    if (args.size == 2) {
                        sender.sendMessage("Settings > recipient := " +
                                UserSettings.getRecipient(sender.uniqueId).toString().toLowerCase())
                        return true
                    }
                    when (args[2]) {
                        "everyone" -> {
                            UserSettings.setRecipient(sender.uniqueId, Recipient.EVERYONE)
                            sender.sendMessage("Settings > recipient = everyone")
                        }
                        "me" -> {
                            UserSettings.setRecipient(sender.uniqueId, Recipient.PLAYER)
                            sender.sendMessage("Settings > recipient = player")
                        }
                        "world" -> {
                            UserSettings.setRecipient(sender.uniqueId, Recipient.WORLD)
                            sender.sendMessage("Settings > recipient = world")
                        }
                        else -> sender.sendMessage("/nb settings recipient [everyone|player|world]")
                    }
                }
                else -> sender.sendMessage("Setting ${args[1]} not exists.")
            }

            return true
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
            if (args.size == 2) return setOf("recipient").filter { s -> s.startsWith(args[1]) }.toMutableList()

            when (args[1]) {
                "recipient" -> {
                    if (args.size == 3) return setOf("me", "world", "everyone").filter { s -> s.startsWith(args[2]) }.toMutableList()
                }
            }

            return mutableListOf()
        }
    }

}
