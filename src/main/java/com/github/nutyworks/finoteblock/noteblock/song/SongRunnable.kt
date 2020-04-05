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

    var world: World? = null
    var player: Player? = null
    lateinit var recipient: Recipient
    var playing: Boolean = false
    private var realTick = -4.0
    private var lastTick = -4
    private var forceEndTime = -1L
    private var bossBar = Bukkit.createBossBar("${song.name} a", BarColor.GREEN, BarStyle.SEGMENTED_10)

    init {

    }

    override fun run() {
        if (playing) {
            realTick += (song.tempo / 100.0) / 20
            if (lastTick > song.songLength) {
                song.stop()
            }

            if (realTick.roundToInt() > lastTick) {
                lastTick = realTick.roundToInt()
                song.notes[lastTick.toShort()]?.forEach { note ->
                    when (recipient) {
                        Recipient.EVERYONE -> Bukkit.getOnlinePlayers().forEach {
                            playNote(it, note)
                        }
                        Recipient.WORLD -> world?.players?.forEach {
                            playNote(it, note)
                        }
                        Recipient.PLAYER -> playNote(player!!, note)
                    }
                    Bukkit.getOnlinePlayers().forEach {
                        updateBossBar(it)
                    }
                }
            }
            forceEndTime = System.currentTimeMillis() + 30000
        } else {
            if (forceEndTime < System.currentTimeMillis()) {
                song.stop()
            }
        }
    }

    private fun playNote(player: Player, note: Note) {
        var inst = if (note.instrumentId < song.customInstrumentStart) {
            MinecraftInstrument.Type.hashMap[note.instrumentId]
        } else {
            song.customInstruments[note.instrumentId]?.soundStr
        }

        if (inst != null) {
            val calcPanning = (note.panning + (song.layers[note.layer]?.panning ?: 100) - 200) / 100.0
            val pannedVector = Vector(sin((player.location.yaw + 90) / 180 * PI).toDouble(), 0.0, -cos((player.location.yaw + 90) / 180 * PI).toDouble()).multiply(calcPanning)

            val finalVolume = (note.volume / 100.0 * (song.layers[note.layer]?.volume ?: 100) / 100.0).toFloat()

            player.playSound(player.location.add(pannedVector), inst, SoundCategory.RECORDS, finalVolume, 2.0.pow((note.key - 45) / 12.0).toFloat())

//            println("$inst ${note.key}")
        }

    }

    private fun updateBossBar(player: Player) {
        when (recipient) {
            Recipient.EVERYONE -> {
                if(!bossBar.players.contains(player)) {
                    bossBar.addPlayer(player)
                }
            }
            Recipient.WORLD -> {
                if(player.world === world) {
                    if(!bossBar.players.contains(player)) {
                        bossBar.addPlayer(player)
                    }
                } else {
                    if(bossBar.players.contains(player)) {
                        bossBar.removePlayer(player)
                    }
                }
            }
            Recipient.PLAYER -> {
                if(player === this.player) {
                    if(!bossBar.players.contains(player)) {
                        bossBar.addPlayer(player)
                    }
                } else {
                    if(bossBar.players.contains(player)) {
                        bossBar.removePlayer(player)
                    }
                }
            }
        }

        bossBar.progress = (if (lastTick < 0) 0 else lastTick).toDouble() / song.songLength
        bossBar.setTitle("${ChatColor.GREEN}${song.name} - ${(bossBar.progress * 100).roundToInt()}%")
        bossBar.color = BarColor.GREEN
    }

    fun pauseBossBar() {
        bossBar.color = BarColor.YELLOW
    }

    fun removeBossBar() {
        bossBar.removeAll()
    }
}
