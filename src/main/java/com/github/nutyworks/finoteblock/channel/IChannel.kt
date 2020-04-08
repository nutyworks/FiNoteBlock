package com.github.nutyworks.finoteblock.channel

import com.github.nutyworks.finoteblock.noteblock.NoteBlockSongManager
import com.github.nutyworks.finoteblock.noteblock.song.NoteBlockSong
import sun.misc.Queue
import java.util.*

interface IChannel {
    val recipient: ArrayList<UUID>
    val name: String
    val queue: Queue<NoteBlockSong>
    val nbsManager: NoteBlockSongManager
    val channelType: ChannelType
    val permanent: Boolean
    var repeat: Boolean

    var playing: NoteBlockSong?
    fun next(force: Boolean = false)
    fun pause()
    fun resume()
    fun stop()
    fun add(song: NoteBlockSong)
}