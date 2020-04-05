package com.github.nutyworks.finoteblock.noteblock.file

import com.github.nutyworks.finoteblock.FiNoteBlockPlugin
import java.io.File
import java.io.FilenameFilter

object FileManager {
    val nbsFiles: List<String>?
        get() = File("${FiNoteBlockPlugin.instance.dataFolder}\\nbs")
                .list { _, name -> name.endsWith(".nbs") }
                ?.map { s -> s.replace(".nbs", "") }
    val userSettings: List<String>?
        get() = File("${FiNoteBlockPlugin.instance.dataFolder}\\settings")
                .list { _, name -> name.endsWith(".dat") }
                ?.map { s -> s.replace(".dat", "")}
}