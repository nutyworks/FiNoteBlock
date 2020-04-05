package com.github.nutyworks.finoteblock.noteblock.song

import com.github.nutyworks.finoteblock.FiNoteBlockPlugin
import com.github.nutyworks.finoteblock.noteblock.Note
import com.github.nutyworks.finoteblock.noteblock.instrument.MinecraftInstrument
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.*

class SongRunnable(val song: NoteBlockSong) : BukkitRunnable() {

    var playing: Boolean = false
    private var realTick = -4.0
    private var lastTick = -4
    private var forceEndTime = -1L
    private val bossBar = Bukkit.createBossBar("_", BarColor.GREEN, BarStyle.SEGMENTED_10)

    init {
        bossBar.progress = 0.0
    }

    override fun run() {
        if (playing) {
            realTick += (song.tempo / 100.0) / 20
            if (lastTick > song.songLength + 1) {
                song.channel.next()
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
                song.channel.next()
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

            player.playSound(player.location.add(pannedVector), inst, SoundCategory.RECORDS, finalVolume, 2.0.pow((note.key - 45) / 12.0).toFloat())

//            println("$inst ${note.key}")
        }

    }

    private fun updateBossBar() {
        Bukkit.getOnlinePlayers().forEach {
            if (song.channel.recipient.contains(it.uniqueId))
                bossBar.addPlayer(it)
            else
                bossBar.removePlayer(it)
        }

        val preCheckProgress = (if (lastTick < 0) 0 else lastTick).toDouble() / song.songLength
        if (preCheckProgress > 1)
            bossBar.progress = 1.0
        else
            bossBar.progress = preCheckProgress
        bossBar.setTitle("${ChatColor.GREEN}${song.name} - ${(bossBar.progress * 100).roundToInt()}%")
        bossBar.color = BarColor.GREEN
    }

    fun pauseBossBar() {
        bossBar.color = BarColor.YELLOW
        bossBar.setTitle("${ChatColor.YELLOW}${song.name} - ${(bossBar.progress * 100).roundToInt()}% [Paused]")
    }

    fun removeBossBar() {
        bossBar.isVisible = false
        bossBar.removeAll()
    }
}
