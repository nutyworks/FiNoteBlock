package com.github.nutyworks.finoteblock.channel

import com.github.nutyworks.finoteblock.noteblock.NoteBlockSongManager
import com.github.nutyworks.finoteblock.noteblock.song.NoteBlockSong
import com.github.nutyworks.finoteblock.util.FailMessage
import sun.misc.Queue
import java.util.*

abstract class BaseChannel : IChannel {
    override val name: String = ""
    override val recipient = ArrayList<UUID>()
    override val queue = Queue<NoteBlockSong>()
    override val nbsManager = NoteBlockSongManager()
    override val channelType = ChannelType.PUBLIC
    override val permanent: Boolean = false
    override var playing: NoteBlockSong? = null

    override fun next(): FailMessage? {
        playing?.stop()

        if (queue.elements().hasMoreElements()) {
            playing = queue.dequeue()

            playing?.load() ?: return FailMessage("Failed to load the song ${playing?.playId ?: "(unknown)"}")
            playing?.play() ?: return FailMessage("Failed to play the song ${playing?.playId ?: "(unknown)"}")
        } else {
            playing = null
        }

        return null
    }

    override fun add(song: NoteBlockSong) {
        queue.enqueue(song)
        song.channel = this

        if (playing == null)
            next()

    }

    override fun pause(): FailMessage? {
        if (playing == null) return FailMessage("No song is playing now.")
        if (playing?.runnable?.playing == false) return FailMessage("The song is not playing.")
        playing?.pause()

        return null
    }

    override fun resume(): FailMessage? {
        if (playing == null) return FailMessage("No song is playing now.")
        if (playing?.runnable?.playing == true) return FailMessage("The song is not paused.")
        playing?.resume()

        return null
    }

    override fun stop(): FailMessage? {
        if (playing == null) return FailMessage("No song to stop.")
        playing?.stop()
        playing = null
        while(queue.elements().hasMoreElements())
            queue.dequeue()


        return null
    }
}