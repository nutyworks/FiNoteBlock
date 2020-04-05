package com.github.nutyworks.finoteblock.command

import com.github.nutyworks.finoteblock.FiNoteBlockPlugin
import com.github.nutyworks.finoteblock.noteblock.file.FileManager
import com.github.nutyworks.finoteblock.noteblock.file.UserSettings
import com.github.nutyworks.finoteblock.noteblock.song.NoteBlockSong
import com.github.nutyworks.finoteblock.noteblock.song.Recipient
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File

class FiNoteBlockCommand : ICommandExecutorCompleter {

    companion object {
        val cmdMap = HashMap<String, ICommandExecutorCompleter>().apply {
            put("list", List())
            put("play", Play())
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
                    sender.sendMessage("============= List [Playing] =============")
                    FiNoteBlockPlugin.nbManager.playing.forEach { (t, u) -> sender.sendMessage("$t. ${u.name}") }
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

            var realPage = page - 1

            if (files != null) {
                if (realPage > files.size / 10) sender.sendMessage("${ChatColor.RED}No page.")
                sender.sendMessage("----- NBS song (Page: ${realPage + 1}/${files.size / 10 + 1}) -----")
                for (i in realPage*10..realPage*10+9) {
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

    class Play : ICommandExecutorCompleter {
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
            } else if (args[1] == "random"){
                file = File("${FiNoteBlockPlugin.instance.dataFolder}\\nbs\\${FileManager.nbsFiles?.random()}.nbs")
            } else {
                sender.sendMessage("/nb play <id|name|random>")
                return true
            }

            if (!file.exists()) {
                sender.sendMessage("File does not exists.")
                return true
            }

            val song = NoteBlockSong(file)
            song.play(UserSettings.getRecipient(sender.uniqueId), player = sender, world = sender.world)
            sender.sendMessage("§6Playing §a${song.name}§6. Play ID is ${song.playId}.")
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
                /* return FileManager.nbsFiles?.toSet()
                        ?.filter {
                            val sp = it.split("\\s")
                            for (i in args.indices - 2) {
                                if (sp[i].startsWith(args[i + 2]))
                                    return@filter true
                                else if (sp[i] == args[i + 2])
                                    continue
                                else
                                    return@filter false
                            }
                            return@filter true
                        }
                        ?.map { targetString ->

                            val sp = targetString.split("\\s")
                            for (i in args.indices - 2) {
                                if (sp[i].startsWith(args[i + 2])) {
                                    val remaining = ""

                                    return@map remaining
                                } else if (sp[i] == args[i + 2])
                                    continue
                                else {
                                    return@map "false"
                                }
                            }

                            return@map targetString + "_returned"
                        }
                        ?.toMutableList() ?: mutableListOf()
              */
            }

            return mutableListOf()
        }
    }

    class Stop : ICommandExecutorCompleter {
        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            if (args.size == 1) {
                sender.sendMessage("/nb stop <playId>")
                return true
            }
            val song = FiNoteBlockPlugin.nbManager.playing[args[1]]
            if (song == null) {
                sender.sendMessage("§cCannot stop ${args[1]}; invalid play id.")
                return true
            }else {
                song.stop()
                sender.sendMessage("§6Stopped §a${song.name}§6.")
            }
            return true
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
            return FiNoteBlockPlugin.nbManager.playing.keys
                    .filter { i -> i.startsWith(args[1]) }
                    .toMutableList()
        }
    }

    class Pause : ICommandExecutorCompleter {
        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            if (args.size == 1) {
                sender.sendMessage("/nb pause <playId>")
                return true
            }

            val song = FiNoteBlockPlugin.nbManager.playing[args[1]]
            if (song == null) {
                sender.sendMessage("§cCannot pause ${args[1]}; invalid play id.")
                return true
            } else if (!song.runnable.playing) {
                sender.sendMessage("§cCannot pause ${args[1]}; song is already paused.")
            } else {
                song.pause()
                sender.sendMessage("§6Paused §a${song.name}§6. §f/noteblock resume ${song.playId} §6to resume.")
            }
            return true
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
            return FiNoteBlockPlugin.nbManager.playing.keys
                    .filter { i -> i.startsWith(args[1]) && FiNoteBlockPlugin.nbManager.getSong(i)?.runnable?.playing!! }
                    .toMutableList()
        }
    }

    class Resume : ICommandExecutorCompleter {
        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
            if (args.size == 1) {
                sender.sendMessage("/nb resume <playId>")
                return true
            }
            val song = FiNoteBlockPlugin.nbManager.playing[args[1]]
            if (song == null) {
                sender.sendMessage("§cCannot resume ${args[1]}; invalid play id.")
                return true
            } else if (song.runnable.playing) {
                sender.sendMessage("§cCannot resume ${args[1]}; song is not paused.")
            } else {
                song.resume()
                sender.sendMessage("§6Resumed §a${song.name}§6. Play ID is ${song.playId}.")
            }
            return true
        }

        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
            return FiNoteBlockPlugin.nbManager.playing.keys
                    .filter { i -> i.startsWith(args[1]) && !FiNoteBlockPlugin.nbManager.getSong(i)?.runnable?.playing!! }
                    .toMutableList()
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
