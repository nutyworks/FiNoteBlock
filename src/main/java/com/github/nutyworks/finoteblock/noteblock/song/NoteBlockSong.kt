@file:Suppress("UnstableApiUsage")

package com.github.nutyworks.finoteblock.noteblock.song

import com.github.nutyworks.finoteblock.FiNoteBlockPlugin
import com.github.nutyworks.finoteblock.noteblock.Layer
import com.github.nutyworks.finoteblock.noteblock.Note
import com.github.nutyworks.finoteblock.noteblock.instrument.CustomInstrument
import com.google.common.io.LittleEndianDataInputStream
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.io.DataInputStream
import java.io.File

class NoteBlockSong(val file: File) {
    var playId = "000000"
    private var isLegacy: Boolean = false
    private var dis: LittleEndianDataInputStream
    var songLength: Short = 0
    var layer: Short = 0
    var name: String = ""
    var tempo: Short = 0
    var currentTick: Short = 0
    var customInstrument: Byte = 0
    var customInstrumentStart: Byte = 16
    val customInstruments: HashMap<Byte, CustomInstrument> = HashMap()
    val notes = HashMap<Short, List<Note>>()
    val layers = HashMap<Short, Layer>()
    val runnable = SongRunnable(this)
    lateinit var task: BukkitTask

    private val debug = false


    init {
        if (!file.path.endsWith(".nbs"))
            throw Exception("Illegal file extension; expected .nbs extension.")

        val temp = DataInputStream(file.inputStream())
        // if open version, it always returns 0.
        // if legacy version, it will returns song length.
        isLegacy = temp.readShort() != 0.toShort()
        temp.close()

//        println(isLegacy)

        dis = LittleEndianDataInputStream(file.inputStream())
        parseNbs()
        if (debug) println("available ${dis.available()}")
        dis.close()
    }

    private fun parseNbs() {
        if (debug) println("===== header =====")
        parseHeader()
        if (debug) println("===== noteblocks =====")
        parseNoteBlocks()
        if (debug) println("===== layer =====")
        parseLayer()
        if (debug) println("===== instruments =====")
        parseCustomInstruments()
        if (debug) println("===== end =====")
    }

    private fun parseHeader() {
        if (debug) println("legacy $isLegacy")
        var temp: Any
        // open version has
        if (!isLegacy) {
            // empty bytes
            temp = dis.readShort()
            if (debug) println("empty $temp")
            // nbs version
            temp = dis.readByte()
            if (debug) println("version $temp")
            // vanilla instrument count
            customInstrumentStart = dis.readByte()
            if (debug) println("customInstrumentStart $customInstrumentStart")
        }
        // song length
        songLength = dis.readShort()
        if (debug) println("songLength $songLength")
        // song layers
        layer = dis.readShort()
        if (debug) println("layer $layer")
        // song name
        name = dis.readString()
        if (name == "")
            name = file.name.replace(".nbs", "")
        if (debug) println("name $name")
        // song author
        temp = dis.readString()
        if (debug) println("author $temp")
        // song original author
        temp = dis.readString()
        if (debug) println("original $temp")
        // song description
        temp = dis.readString()
        if (debug) println("desc $temp")
        // song tempo
        tempo = dis.readShort()
        if (debug) println("tempo $tempo")
        // auto-saving
        temp = dis.readByte()
        if (debug) println("auto-save $temp")
        // auto-saving duration
        temp = dis.readByte()
        if (debug) println("^ duration $temp")
        // time signature
        temp = dis.readByte()
        if (debug) println("time-sign $temp")
        // minutes spent
        temp = dis.readInt()
        if (debug) println("min-spent $temp")
        // left clicks
        temp = dis.readInt()
        if (debug) println("left $temp")
        // right clicks
        temp = dis.readInt()
        if (debug) println("right $temp")
        // note blocks added
        temp = dis.readInt()
        if (debug) println("note-added $temp")
        // note blocks removed
        temp = dis.readInt()
        if (debug) println("note-removed $temp")
        // midi/schematic file name
        temp = dis.readString()
        if (debug) println("midi-file-name $temp")
        if (!isLegacy) {
            // loop on/off
            temp = dis.readByte()
            if (debug) println("loop $temp")
            // max loop count
            temp = dis.readByte()
            if (debug) println("max-loop $temp")
            // loop start tick
            temp = dis.readShort()
            if (debug) println("loop-tick $temp")
        }
    }

    private fun parseNoteBlocks() {
        var tick: Short = -1 // searching tick
        var jumps: Short // jump tick

        while (true) {
            jumps = dis.readShort()
            // if jump is 0, it is end of note block.
            if (jumps == 0.toShort()) break
            tick = (tick + jumps).toShort()

            // layer starts at -1.
            var currentLayer: Short = -1

            val notesInTick = ArrayList<Note>()
            while (true) {
                // THIS IS LAYER JUMPS
                // if layer jump is 0, continue to next tick.
                jumps = dis.readShort()
                if (jumps == 0.toShort()) break

                currentLayer = (currentLayer + jumps).toShort()
                val instrument: Byte = dis.readByte()
                val key: Byte = dis.readByte()
                if (!isLegacy) {
                    val volume = dis.readByte()
                    val panning = dis.readByte()
                    /* pitch */ dis.readShort()

                    notesInTick.add(Note(currentLayer, instrument, key, volume, panning))
                } else {
                    notesInTick.add(Note(currentLayer, instrument, key))
                }
            }
            if (debug) println("$tick = $notesInTick")
            notes[tick] = notesInTick
//            if (debug) println(notes)
        }
/*
        while (toNextTick != 0.toShort()) {

            while (jumps != 0.toShort()) {
                // jumps to next tick
                toNextTick = dis.readShort()
                // jumps to next layer PRIORITY
                jumps = dis.readShort()
                // instrument
                val instrument = dis.readByte()
                val key = dis.readByte()
                if (!isLegacy) {
                    val volume = dis.readByte()
                    val panning = dis.readByte()
                    dis.readShort()

                    notesInTick.add(Note(instrument, key, volume, panning))
                } else {
                    notesInTick.add(Note(instrument, key))
                }
            }
            tick = (tick + toNextTick).toShort()
            notes[tick] = notesInTick
        }
 */

    }

    private fun parseLayer() {
        var temp: Any
        for (i in 0 until layer) {
            if (debug) println("layer $i")
            // name
            temp = dis.readString()
            if (debug) println("^ name $temp")
            if (!isLegacy) {
                // locked
                temp = dis.readByte()
                if (debug) println("^ locked $temp")
            }
            val volume = dis.readByte()
            if (debug) println("^ volume $volume")
            if (!isLegacy) {
                val panning = dis.readByte()
                if (debug) println("^ panning $panning")
                layers[i.toShort()] = Layer(volume, panning)
            } else {
                layers[i.toShort()] = Layer(volume)
            }
        }
    }

    private fun parseCustomInstruments() {
        var temp: Any
        customInstrument = dis.readByte()
        if (debug) println("custom-instrument-count $customInstrument")
        for (i in 0 until customInstrument) {
            if (debug) println("custom-inst $i")
            val name = dis.readString()
            if (debug) println("^ name $name")
            // file
            temp = dis.readString()
            if (debug) println("^ file $temp")
            val pitch = dis.readByte()
            if (debug) println("^ pitch $pitch")
            // press key?
            temp = dis.readByte()
            if (debug) println("press-piano $temp")

            customInstruments[(i + customInstrumentStart).toByte()] = CustomInstrument(name, pitch)
        }

        if (debug) println("custom-instruments $customInstruments")
    }

    fun play(recipient: Recipient, player: Player? = null, world: World? = null) {
        runnable.recipient = recipient
        runnable.player = player
        runnable.world = world

        when (recipient) {
            Recipient.WORLD -> if (world == null) throw IllegalArgumentException("World must be specified.")
            Recipient.PLAYER -> if (player == null) throw java.lang.IllegalArgumentException("Player must be specified.")
        }

        playId = FiNoteBlockPlugin.nbManager.register(this)
        task = runnable.runTaskTimer(FiNoteBlockPlugin.instance, 0, 1)
        runnable.playing = true
//        println(notes)
    }

    fun stop() {
        task.cancel()
        runnable.playing = false
        FiNoteBlockPlugin.nbManager.unregister(playId)
        runnable.removeBossBar()
    }

    fun pause() {
        runnable.playing = false
        runnable.pauseBossBar()
    }

    fun resume() {
        runnable.playing = true
    }
}

private fun LittleEndianDataInputStream.readString(): String {
    val length = readInt()

    var str = ""
    for (i in 0 until length)
        str += readByte().toChar()

    return str
}
