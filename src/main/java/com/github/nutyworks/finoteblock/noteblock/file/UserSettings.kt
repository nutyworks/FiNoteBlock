package com.github.nutyworks.finoteblock.noteblock.file

import com.github.nutyworks.finoteblock.FiNoteBlockPlugin
import com.github.nutyworks.finoteblock.noteblock.song.Recipient
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.util.*

object UserSettings {
    /**
     * 1. Byte: Recipient
     * 2. Boolean: BossBar
     */

    fun getRecipient(uuid: UUID): Recipient {
        val file = File("${FiNoteBlockPlugin.instance.dataFolder}\\userdata\\$uuid.dat")
        if (!file.exists()) file.createNewFile()
        val dis = DataInputStream(file.inputStream())

        if (dis.available() > 0) {
            val v = dis.readByte()
            dis.close()
            return when (v) {
                0.toByte() -> Recipient.EVERYONE
                1.toByte() -> Recipient.WORLD
                2.toByte() -> Recipient.PLAYER
                else -> Recipient.PLAYER
            }
        }

        return Recipient.PLAYER
    }

    fun setRecipient(uuid: UUID, value: Recipient) {
        val file = File("${FiNoteBlockPlugin.instance.dataFolder}\\userdata\\$uuid.dat")
        if (!file.exists()) file.createNewFile()
        val dos = DataOutputStream(file.outputStream())

        dos.writeByte(value.ordinal)

        dos.close()
    }

    fun getBossBarVisible(uuid: UUID): Recipient {
        val file = File("${FiNoteBlockPlugin.instance.dataFolder}\\userdata\\$uuid.dat")
        if (!file.exists()) file.createNewFile()
        val dis = DataInputStream(file.inputStream())

        if (dis.available() > 0) {
            val v = dis.readByte()
            dis.close()
            return when (v) {
                0.toByte() -> Recipient.EVERYONE
                1.toByte() -> Recipient.WORLD
                2.toByte() -> Recipient.PLAYER
                else -> Recipient.PLAYER
            }
        }

        return Recipient.PLAYER
    }

    fun setBossBarVisible(uuid: UUID, value: Recipient) {
        val file = File("${FiNoteBlockPlugin.instance.dataFolder}\\userdata\\$uuid.dat")
        if (!file.exists()) file.createNewFile()
        val dos = DataOutputStream(file.outputStream())

        dos.writeByte(value.ordinal)

        dos.close()
    }
}