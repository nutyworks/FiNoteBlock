package com.github.nutyworks.finoteblock.noteblock.file

import com.github.nutyworks.finoteblock.FiNoteBlockPlugin
import com.github.nutyworks.finoteblock.command.CommandFailException
import com.github.nutyworks.finoteblock.util.HashBijective
import com.github.nutyworks.finoteblock.util.hashBijectiveOf
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

class UserSettings(val uuid: UUID) {
    /**
     * 1. Byte: Recipient
     * 2. Boolean: BossBar
     */

    // (key, (offset, value))
    companion object {
        val default = HashMap<String, SettingOption>().apply {
            put("displayBossBar", SettingOption(true, hashBijectiveOf<Any, Any>().apply {
                set(primary = true, secondary = true)
                set(primary = false, secondary = false)
            }))
            put("bossBarProgress", SettingOption("percentage", hashBijectiveOf<Any, Any>().apply {
                set("percentage", 0.toByte())
                set("time", 1.toByte())
                set("hide", 2.toByte())
            }))
        }

        val settingIndex = ArrayList<String>().apply {
            add("displayBossBar")
            add("bossBarProgress")
        }
    }

    private val settings = HashMap<String, Any>()
    private val file = File("${FiNoteBlockPlugin.instance.dataFolder}\\userdata\\$uuid.dat")

    init {
        if (!file.exists()) create()
        load()
    }

    fun create() {
        file.createNewFile()
        save()
    }

    fun load() {
        val fis = file.inputStream()
        val dis = DataInputStream(fis)

        settingIndex.forEach {
            val type = default[it]?.acceptableValues?.secondaryToPrimary?.keys?.first()?.javaClass?.typeName
            if (dis.available() > 0) {
                val value = when (type) {
                    "java.lang.Boolean" -> dis.readBoolean()
                    "java.lang.Byte" -> dis.readByte()
                    "java.lang.Short" -> dis.readShort()
                    "java.lang.Integer" -> dis.readInt()
                    "java.lang.Long" -> dis.readLong()
                    "java.lang.String" -> dis.readUTF()
                    else -> default[it]?.defaultValue
                } as Any
                settings[it] = default[it]?.acceptableValues?.secondaryToPrimary?.get(value) as Any
            } else {
                settings[it] = default[it]?.defaultValue as Any
            }
        }

        dis.close()
        fis.close()
    }

    fun save() {
        val file = File("${FiNoteBlockPlugin.instance.dataFolder}\\userdata\\$uuid.dat")
        val fis = file.outputStream()
        val dos = DataOutputStream(fis)

        settingIndex.forEach {
            when (val value = default[it]?.acceptableValues?.primaryToSecondary?.get(settings[it]
                    ?: default[it]?.defaultValue)) {
                is Boolean -> dos.writeBoolean(value)
                is Byte -> dos.writeByte(value.toInt())
                is Short -> dos.writeShort(value.toInt())
                is Int -> dos.writeInt(value)
                is Long -> dos.writeLong(value)
                is String -> dos.writeUTF(value)
            }
        }

        dos.close()
        fis.close()
    }

    fun get(key: String): Any? {
        if (!default.containsKey(key)) throw CommandFailException("Settings $key not exists.")
        return settings[key]
    }

    fun set(key: String, value: Any) {

        if (!default.containsKey(key)) throw CommandFailException("Settings $key not exists.")

        val convertedValue = when (default[key]?.defaultValue?.javaClass?.typeName) {
            "java.lang.Boolean" -> when (value.toString().toLowerCase()) {
                "true" -> true
                "false" -> false
                else -> throw CommandFailException("Invalid value.")
            }
            "java.lang.Byte" -> value.toString().toByte()
            "java.lang.Short" -> value.toString().toShort()
            "java.lang.Integer" -> value.toString().toInt()
            "java.lang.Long" -> value.toString().toLong()
            "java.lang.String" -> value.toString()
            else -> throw CommandFailException("Invalid value.")
        }

        if (default[key]?.acceptableValues?.primaryToSecondary?.keys?.contains(convertedValue) == true) {
            settings[key] = convertedValue
        } else {
            throw CommandFailException("Invalid value.")
        }

        save()
    }
}

class SettingOption(val defaultValue: Any, val acceptableValues: HashBijective<Any, Any>)