package com.github.nutyworks.finoteblock.noteblock.song

import com.github.nutyworks.finoteblock.FiNoteBlockPlugin
import com.github.nutyworks.finoteblock.noteblock.Note
import com.github.nutyworks.finoteblock.noteblock.instrument.MinecraftInstrument
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.*

class SongRunnable(val song: NoteBlockSong) : BukkitRunnable() {

    var playing: Boolean = false
    private var realTick = -4.0
    private var lastTick = -4
    private var forceEndTime = -1L
    private val percentageBossBar = Bukkit.createBossBar("_", BarColor.GREEN, BarStyle.SEGMENTED_10)
    private val emptyBossBar = Bukkit.createBossBar("_", BarColor.GREEN, BarStyle.SEGMENTED_10)
    private val timeBossBar = Bukkit.createBossBar("_", BarColor.GREEN, BarStyle.SEGMENTED_10)
    private var totalPlayTime = (song.songLength / (song.tempo / 100.0)).toInt()
    private var currentPlayTime = 0

    init {
        percentageBossBar.progress = 0.0
        emptyBossBar.progress = 0.0
        timeBossBar.progress = 0.0
    }

    override fun run() {
        if (playing) {
            realTick += (song.tempo / 100.0) / 20
            if (lastTick > song.songLength + 1) {
                song.channel.next(true)
            }

            if (realTick.roundToInt() > lastTick) {
                lastTick = realTick.roundToInt()
                song.notes[lastTick.toShort()]?.forEach { note ->
                    song.channel.recipient.forEach {
                        val player = Bukkit.getPlayer(it)
                        if (player != null)
                            playNote(player, note)
                    }
                }
            }

            forceEndTime = System.currentTimeMillis() + 30000
            updateBossBar()
        } else {
            if (forceEndTime < System.currentTimeMillis()) {
                song.channel.next(true)
            }
        }
    }

    private fun playNote(player: Player, note: Note) {
        val inst = if (note.instrumentId < song.customInstrumentStart) {
            MinecraftInstrument.Type.hashMap[note.instrumentId]
        } else {
            song.customInstruments[note.instrumentId]?.soundStr
        }

        if (inst != null) {
            val calcPanning = (note.panning + (song.layers[note.layer]?.panning ?: 100) - 200) / 100.0
            val pannedVector = Vector(sin((player.location.yaw + 90) / 180 * PI), 0.0, -cos((player.location.yaw + 90) / 180 * PI)).multiply(calcPanning)

            val finalVolume = (note.volume / 100.0 * (song.layers[note.layer]?.volume ?: 100) / 100.0).toFloat()

            val modifier = when {
                note.key < 33 -> "_-1"
                note.key > 57 -> "_1"
                else -> ""
            }
            val pitch: Float = when {
                note.key < 33 -> 2.0.pow((note.key - 21) / 12.0).toFloat()
                note.key > 57 -> 2.0.pow((note.key - 69) / 12.0).toFloat()
                note.key < 9 -> return
                note.key > 81 -> return
                else -> 2.0.pow((note.key - 45) / 12.0).toFloat()
            }

            player.playSound(player.location.add(pannedVector), inst + modifier, SoundCategory.RECORDS, finalVolume, pitch)
        }

    }

    private fun updateBossBar() {
        totalPlayTime = (song.songLength / (song.tempo / 100.0)).toInt()
        currentPlayTime = (realTick / (song.tempo / 100.0)).toInt()
        Bukkit.getOnlinePlayers().forEach {
            val isListeningToChannel = song.channel.recipient.contains(it.uniqueId)
            val displayBossBar = FiNoteBlockPlugin.instance.playerManager.playerSetting[it.uniqueId]?.get("displayBossBar") == true
            val bossBarType = FiNoteBlockPlugin.instance.playerManager.playerSetting[it.uniqueId]?.get("bossBarProgress")
            if (isListeningToChannel && displayBossBar) {
                when (bossBarType) {
                    "percentage" -> {
                        percentageBossBar.addPlayer(it)
                        emptyBossBar.removePlayer(it)
                        timeBossBar.removePlayer(it)
                    }
                    "time" -> {
                        percentageBossBar.removePlayer(it)
                        emptyBossBar.removePlayer(it)
                        timeBossBar.addPlayer(it)
                    }
                    "hide" -> {
                        percentageBossBar.removePlayer(it)
                        emptyBossBar.addPlayer(it)
                        timeBossBar.removePlayer(it)
                    }
                }
            } else {
                percentageBossBar.removePlayer(it)
                emptyBossBar.removePlayer(it)
                timeBossBar.removePlayer(it)
            }
        }

        val preCheckProgress = (if (lastTick < 0) 0 else lastTick).toDouble() / song.songLength
        if (preCheckProgress > 1) {
            percentageBossBar.progress = 1.0
            emptyBossBar.progress = 1.0
            timeBossBar.progress = 1.0
        } else {
            percentageBossBar.progress = preCheckProgress
            emptyBossBar.progress = preCheckProgress
            timeBossBar.progress = preCheckProgress
        }
        percentageBossBar.setTitle("${ChatColor.GREEN}${song.name} §7[${(percentageBossBar.progress * 100).roundToInt()}%]")
        percentageBossBar.color = BarColor.GREEN
        emptyBossBar.setTitle("${ChatColor.GREEN}${song.name}")
        emptyBossBar.color = BarColor.GREEN
        timeBossBar.setTitle("${ChatColor.GREEN}${song.name} §7[${convertToFormattedTime(currentPlayTime)}/${convertToFormattedTime(totalPlayTime)}]")
        timeBossBar.color = BarColor.GREEN
    }

    fun pauseBossBar() {
        percentageBossBar.color = BarColor.YELLOW
        percentageBossBar.setTitle("${ChatColor.YELLOW}${song.name} [Paused] §7[${(percentageBossBar.progress * 100).roundToInt()}]")
        emptyBossBar.color = BarColor.YELLOW
        emptyBossBar.setTitle("${ChatColor.YELLOW}${song.name} [Paused]")
        timeBossBar.color = BarColor.YELLOW
        timeBossBar.setTitle("${ChatColor.YELLOW}${song.name} [Paused] §7[${convertToFormattedTime(currentPlayTime)}/${convertToFormattedTime(totalPlayTime)}]")
    }

    fun removeBossBar() {
        percentageBossBar.isVisible = false
        percentageBossBar.removeAll()
        emptyBossBar.isVisible = false
        emptyBossBar.removeAll()
        timeBossBar.isVisible = false
        timeBossBar.removeAll()
    }

    private fun convertToFormattedTime(second: Int): String {
        return "%d:%02d".format(second / 60, second % 60)
    }
}
