package com.github.nutyworks.finoteblock

import com.github.nutyworks.finoteblock.channel.ChannelManager
import com.github.nutyworks.finoteblock.channel.DefaultChannel
import com.github.nutyworks.finoteblock.command.FiNoteBlockCommand
import com.github.nutyworks.finoteblock.noteblock.NoteBlockSongManager
import com.github.nutyworks.finoteblock.noteblock.file.FileManager
import com.github.nutyworks.finoteblock.noteblock.file.UserSettings
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class FiNoteBlockPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: FiNoteBlockPlugin
        val fileManager = FileManager
    }

    val channelManager = ChannelManager()
    val playerManager = PlayerManager()

    override fun onEnable() {
        instance = this

        getCommand("nb")?.let {
            it.setExecutor(FiNoteBlockCommand(this))
            it.tabCompleter = FiNoteBlockCommand(this)
        }

        server.pluginManager.registerEvents(playerManager, instance)

        val nbsFolder = File("${dataFolder.path}\\nbs")
        val userDataFolder = File("${dataFolder.path}\\userdata")

        if (!nbsFolder.exists()) nbsFolder.mkdirs()
        if (!userDataFolder.exists()) userDataFolder.mkdirs()

        Bukkit.getOnlinePlayers().forEach {
            playerManager.moveChannel(it.uniqueId, "default")
            playerManager.playerSetting[it.uniqueId] = UserSettings(it.uniqueId)
        }
    }
}