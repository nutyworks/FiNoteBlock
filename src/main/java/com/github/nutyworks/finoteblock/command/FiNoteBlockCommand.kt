package com.github.nutyworks.finoteblock.command

import com.github.nutyworks.finoteblock.FiNoteBlockPlugin
import com.github.nutyworks.finoteblock.channel.ChannelType
import com.github.nutyworks.finoteblock.noteblock.file.FileManager
import com.github.nutyworks.finoteblock.noteblock.file.UserSettings
import com.github.nutyworks.finoteblock.noteblock.song.NoteBlockSong
import com.github.nutyworks.finoteblock.noteblock.song.Recipient
import com.github.nutyworks.finoteblock.util.join
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File
import java.util.regex.Pattern.matches
import kotlin.collections.HashMap

class FiNoteBlockCommand(val plugin: FiNoteBlockPlugin) : AbstractExecutorCompleter() {

    companion object {
        val cmdMap = HashMap<String, AbstractExecutorCompleter>().apply {
            put("add", Add())
            put("help", Help())
            put("list", List())
            put("next", Next())
            put("stop", Stop())
            put("pause", Pause())
            put("resume", Resume())
            put("channel", Channel())
            put("settings", Settings())
        }
    }

    override fun command() {
        if (args.isEmpty()) throw CommandFailException("Invalid usage. '/$label help' to get information.")

        cmdMap[args[0]]?.onCommand(sender, command, label, args)
                ?: throw CommandFailException("Invalid usage. '/$label help' to get information.")
    }

    override fun tabComplete(): MutableList<String> {
        return if (args.size == 1)
            cmdMap.keys.filter { it.startsWith(args[0]) }.toMutableList()
        else
            cmdMap[args[0]]?.onTabComplete(sender, command, alias, args) ?: mutableListOf()
    }

    /* SUB COMMANDS */

    class Help : AbstractExecutorCompleter() {
        override fun command() {
            cmdMap.forEach { sender.sendMessage("§6/$label ${it.key}") }
        }

        override fun tabComplete(): MutableList<String> {
            return mutableListOf()
        }

    }

    class List : AbstractExecutorCompleter() {
        override fun command() {
            if (args.size == 1) {
                playableList(1)
            } else when {
                args[1] == "playable" -> { // nb list playable [page]
                    if (args.size >= 3) {
                        if (args[2].matches(Regex("^\\d+$"))) {
                            playableList(args[2].toInt())
                        } else throw CommandFailException("Page must be an integer.")
                    } else {
                        playableList(1)
                    }
                }
                args[1] == "playing" -> { // nb list playing [page]
                    if (sender !is Player) throw PlayerOnlyCommandException()

                    // get player's channel
                    val channel = FiNoteBlockPlugin.instance.playerManager.playerChannel[player?.uniqueId]
                            ?: throw CommandFailException("Could not find ${player?.name}'s channel.")

                    // send playing and queued song in player's channel
                    sender.sendMessage("§aNow playing in §6${channel.name}§a: \n §a${channel.playing?.name ?: "§7None"}")
                    sender.sendMessage("§aQueued in §6${channel.name}§a: " +
                            if (channel.queue.reverseElements().hasMoreElements()) "" else "\n §7None")
                    channel.queue.reverseElements().toList().forEachIndexed { index, song ->
                        sender.sendMessage(" §6$index. §a${song.name} §7(${song.playId})")
                    }
                }
                args[1].matches(Regex("^\\d+$")) -> { // nb list [page]
                    playableList(args[1].toInt())
                }
                else -> throw CommandFailException("Usage: /$label list [playing|playble]")
            }
        }

        private fun playableList(page: Int, sender: CommandSender = this.sender) {
            if (page <= 0) throw CommandFailException("No page.")
            val realPage = page - 1

            val files = FileManager.nbsFiles ?: throw CommandFailException("No song in ./plugin/FiNoteBlock/nbs/")

            if (realPage > files.size / 10) throw CommandFailException("No page.")

            sender.sendMessage("----- NBS song (Page: ${realPage + 1}/${files.size / 10 + 1}) -----")
            for (i in realPage * 10..realPage * 10 + 9) {
                if (files.size <= i)
                    break
                sender.sendMessage("${ChatColor.GOLD}$i. ${ChatColor.GREEN}${files[i]}")
            }

        }

        override fun tabComplete(): MutableList<String> {
            return if (args.size == 2) {
                setOf("playing", "playable").filter { it.startsWith(args[1]) }.toMutableList()
            } else mutableListOf()
        }
    }

    class Add : AbstractExecutorCompleter() {
        override fun command() {
            if (sender !is Player) throw PlayerOnlyCommandException()

            if (args.size == 1) throw CommandFailException("Usage: /$label add <id|name|random>")

            val file = when (args[1]) {
                "id" -> {
                    if (args.size == 3)
                        File("${FiNoteBlockPlugin.instance.dataFolder}\\nbs\\${FileManager.nbsFiles?.get(args[2].toInt())}.nbs")
                    else throw CommandFailException("Usage: /$label add id [id]")
                }
                "name" -> {
                    if (args.size >= 3) {
                        File("${FiNoteBlockPlugin.instance.dataFolder}\\nbs\\${args.join(begin = 2)}.nbs")
                    }
                    else throw CommandFailException("Usage: /$label add name [song]")
                }
                "random" -> File("${FiNoteBlockPlugin.instance.dataFolder}\\nbs\\${FileManager.nbsFiles?.random()}.nbs")
                else -> throw CommandFailException("Usage: /$label add <id|name|random>")
            }

            if (!file.exists()) throw CommandFailException("Song does not exists.")

            val song = NoteBlockSong(file)
            val channel = FiNoteBlockPlugin.instance.playerManager.playerChannel[player?.uniqueId]
                    ?: throw CommandFailException("Could not find ${player?.name}'s channel.")

            channel.add(song)
            sender.sendMessage("§6Queued §a${song.name}§6 in ${channel.name}. Play ID is ${song.playId}.")
        }

        override fun tabComplete(): MutableList<String> {
            return when {
                args.size == 2 -> setOf("id", "name", "random").filter { it.startsWith(args[1]) }.toMutableList()
                args.size >= 3 -> {
                    when (args[1]) {
                        "id" -> {
                            if (args.size == 3) {
                                FileManager.nbsFiles?.indices?.toSet()
                                        ?.filter { i -> i.toString().startsWith(args[2]) }
                                        ?.map { i -> i.toString() }
                                        ?.toMutableList() ?: mutableListOf()
                            } else mutableListOf()
                        }
                        "name" -> {
                            FileManager.nbsFiles?.toSet()
                                    ?.filter { it.startsWith(args.join(begin = 2)) }
                                    ?.map { it.split(" ").toTypedArray().join(begin = args.size - 3) }
                                    ?.toMutableList() ?: mutableListOf()
                        }
                        else -> mutableListOf()
                    }
                }
                else -> mutableListOf()
            }
        }
    }

    class Next : AbstractExecutorCompleter() {

        override fun command() {
            if (sender !is Player) throw PlayerOnlyCommandException()

            FiNoteBlockPlugin.instance.playerManager.playerChannel[player?.uniqueId]!!.next()
            sender.sendMessage("§6Playing the next song.")
        }

        override fun tabComplete(): MutableList<String> {
            return mutableListOf()
        }

    }

    class Stop : AbstractExecutorCompleter() {

        override fun command() {
            if (sender !is Player) throw PlayerOnlyCommandException()

            FiNoteBlockPlugin.instance.playerManager.playerChannel[player?.uniqueId]!!.stop()
            sender.sendMessage("§6Stopped playing song and removed all queued song.")
        }

        override fun tabComplete(): MutableList<String> {
            return mutableListOf()
        }
    }

    class Pause : AbstractExecutorCompleter() {

        override fun command() {
            if (sender !is Player) throw PlayerOnlyCommandException()

            FiNoteBlockPlugin.instance.playerManager.playerChannel[player?.uniqueId]!!.pause()
            sender.sendMessage("§6Song paused. §f/$label resume §6to resume.")
        }

        override fun tabComplete(): MutableList<String> {
            return mutableListOf()
        }
    }

    class Resume : AbstractExecutorCompleter() {

        override fun command() {
            if (sender !is Player) throw PlayerOnlyCommandException()

            FiNoteBlockPlugin.instance.playerManager.playerChannel[player?.uniqueId]!!.resume()
            sender.sendMessage("§6Song resumed. §f/$label pause §6to pause.")
        }

        override fun tabComplete(): MutableList<String> {
            return mutableListOf()
        }
    }

    class Channel : AbstractExecutorCompleter() {
        override fun command() {
            if (sender !is Player) throw PlayerOnlyCommandException()

            if (args.size == 1) throw CommandFailException("Usage: /$label channel <create|join|list|remove|settings>")

            when (args[1]) {
                "create" -> {
                    if (args.size == 3) {
                        FiNoteBlockPlugin.instance.channelManager.addChannel(args[2], ChannelType.PUBLIC)
                        sender.sendMessage("§6Successfully created §a${args[2]} §6channel.")
                    }
                    else throw CommandFailException("Usage: /$label channel create <name>")
                }
                "join" -> {
                    if (args.size == 3) {
                        FiNoteBlockPlugin.instance.playerManager.moveChannel(player!!.uniqueId, args[2])
                    }
                    else throw CommandFailException("Usage: /$label channel join <name>")
                }
                "list" -> {
                    sender.sendMessage("§6Channels:")
                    FiNoteBlockPlugin.instance.channelManager.channels.forEach {
                        sender.sendMessage(" §a${it.key}§6 - Currently playing: §a${it.value.playing?.name ?: "§7None"}")
                    }
                }
                "remove" -> {
                    if (args.size == 3) {
                        FiNoteBlockPlugin.instance.channelManager.removeChannel(args[2])
                        sender.sendMessage("§6Removed §a${args[2]} §6channel.")
                    }
                    else throw CommandFailException("Usage: /$label channel remove <name>")
                }
                "settings" -> {
                    sender.sendMessage("This command is under development.")
                }
                else -> throw CommandFailException("Usage: /$label channel <create|join|list|settings>")
            }
        }


        override fun tabComplete(): MutableList<String> {
            return when (args.size) {
                2 -> setOf("create", "join", "settings", "remove", "list").filter { s -> s.startsWith(args[1]) }.toMutableList()
                3 -> {
                    if (args[1] == "join" || args[1] == "remove") {
                        FiNoteBlockPlugin.instance.channelManager.channels.keys.filter { k -> k.startsWith(args[2]) }
                                .toMutableList()
                    } else {
                        mutableListOf()
                    }
                }
                else -> mutableListOf()
            }
        }
    }

    class Settings : AbstractExecutorCompleter() {

        override fun command() {
            if (sender !is Player) throw PlayerOnlyCommandException()

            when (args.size) { // set and display at the same time
                2 -> sender.sendMessage("${args[1]} is ${setting(args[1])}.")
                3 -> sender.sendMessage("${args[1]} set to ${setting(args[1], args[2])}.")
                else -> throw CommandFailException("Usage: /$label settings <setting>")
            }
        }

        fun setting(setting: String, value: String? = null): Any? {
            if (value != null) {
                FiNoteBlockPlugin.instance.playerManager.playerSetting[player?.uniqueId]?.set(setting, value)
            }

            return Pair(setting, FiNoteBlockPlugin.instance.playerManager.playerSetting[player?.uniqueId]?.get(setting)).second
        }

        override fun tabComplete(): MutableList<String> {
            return when (args.size) {
                2 -> UserSettings.default.keys.filter { s -> s.startsWith(args[1]) }.toMutableList()
                3 -> UserSettings.default[args[1]]?.acceptableValues?.primaryToSecondary?.keys?.map { e -> e.toString() }
                        ?.filter { s -> s.startsWith(args[2]) }?.toMutableList() ?: mutableListOf()
                else -> mutableListOf()
            }
        }
    }
}
