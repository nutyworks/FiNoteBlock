package com.github.nutyworks.finoteblock.channel

import com.github.nutyworks.finoteblock.noteblock.NoteBlockSongManager
import com.github.nutyworks.finoteblock.noteblock.song.NoteBlockSong
import com.github.nutyworks.finoteblock.util.FailMessage
import sun.misc.Queue
import java.util.*

interface IChannel {
    val recipient: ArrayList<UUID>
    val name: String
    val queue: Queue<NoteBlockSong>
    val nbsManager: NoteBlockSongManager
    val channelType: ChannelType
    val permanent: Boolean

    var playing: NoteBlockSong?
    fun next(): FailMessage?
    fun pause(): FailMessage?
    fun resume(): FailMessage?
    fun stop(): FailMessage?

    fun add(song: NoteBlockSong)
}