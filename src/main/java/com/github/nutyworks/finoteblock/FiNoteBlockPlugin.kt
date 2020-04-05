package com.github.nutyworks.finoteblock

import com.github.nutyworks.finoteblock.command.FiNoteBlockCommand
import com.github.nutyworks.finoteblock.noteblock.NoteBlockSongManager
import com.github.nutyworks.finoteblock.noteblock.file.FileManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class FiNoteBlockPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: FiNoteBlockPlugin
        val nbManager = NoteBlockSongManager()
        val fileManager = FileManager
    }

    override fun onEnable() {
        instance = this

        getCommand("nb")?.let {
            it.setExecutor(FiNoteBlockCommand())
            it.tabCompleter = FiNoteBlockCommand()
        }

        val nbsFolder = File("${dataFolder.path}\\nbs")
        val userDataFolder = File("${dataFolder.path}\\userdata")

        if (!nbsFolder.exists()) nbsFolder.mkdirs()
        if (!userDataFolder.exists()) userDataFolder.mkdirs()
    }
}