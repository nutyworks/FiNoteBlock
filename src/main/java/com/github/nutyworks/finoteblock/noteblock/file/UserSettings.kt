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
            //
            put("displayBossBar", SettingOption(2, true, hashBijectiveOf<Any, Any>().apply {
                set(true, secondary = true)
                set(false, secondary = false)
            }))
            put("bossBarProgress", SettingOption(3, "percentage", hashBijectiveOf<Any, Any>().apply {
                set("percentage", true)
                set("time", false)
            }))
        }
    }

    private val settings = HashMap<String, Any>()
    private val file = File("${FiNoteBlockPlugin.instance.dataFolder}\\userdata\\$uuid.dat")

    init {
        if (!file.exists()) create()
    }

    fun create() {
        file.createNewFile()
        save()
        load()
    }

    fun load() {
        val fis = file.inputStream()
        val dis = DataInputStream(fis)



        dis.close()
        fis.close()
        TODO()
    }

    fun save() {
        val file = File("${FiNoteBlockPlugin.instance.dataFolder}\\userdata\\$uuid.dat")
        val fis = file.outputStream()
        val dos = DataOutputStream(fis)

        dos.close()
        fis.close()
        TODO()
    }

    fun get(key: String): Any? {
        if (!default.containsKey(key)) throw CommandFailException("Settings $key not exists.")
        return settings[key]
    }

    fun set(key: String, value: Any) {

//        println("set() call")

        if (!default.containsKey(key)) throw CommandFailException("Settings $key not exists.")

        val convertedValue = when (default[key]?.defaultValue?.javaClass?.typeName) {
            "java.lang.Boolean" -> when(value.toString().toLowerCase()) {
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

//        println("${convertedValue.javaClass.typeName} != ${default[key]?.defaultValue?.javaClass?.typeName}")
//        println(convertedValue)
//        println(convertedValue.javaClass.typeName)

        if (default[key]?.acceptableValues?.primaryToSecondary?.keys?.contains(convertedValue) == true)
            settings[key] = convertedValue
    }
}

class SettingOption(val offset: Int, val defaultValue: Any, val acceptableValues: HashBijective<Any, Any>)