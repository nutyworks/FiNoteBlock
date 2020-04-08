package com.github.nutyworks.finoteblock.channel

import com.github.nutyworks.finoteblock.command.CommandFailException
import com.github.nutyworks.finoteblock.noteblock.NoteBlockSongManager
import com.github.nutyworks.finoteblock.noteblock.song.NoteBlockSong
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
    override var repeat: Boolean = false

    override fun next(force: Boolean /* = false */) {

        if (repeat && playing != null) {
            playing?.file?.let { file -> add(NoteBlockSong(file)) }
        }

        val hasMore = queue.elements().hasMoreElements()
        if (force || hasMore) {
            playing?.stop()
            playing = null
        }
        if (hasMore) {
            playing = queue.dequeue()

            playing?.load()
            playing?.play()
        } else if (!force) throw CommandFailException("There is no queued song to play.")

    }

    override fun add(song: NoteBlockSong) {
        queue.enqueue(song)
        song.channel = this

        if (playing == null)
            next()

    }

    override fun pause() {
        if (playing == null) throw CommandFailException("No song is playing now.")
        if (playing?.runnable?.playing == false) throw CommandFailException("The song is not playing.")
        playing?.pause()
    }

    override fun resume() {
        if (playing == null) throw CommandFailException("No song is playing now.")
        if (playing?.runnable?.playing == true) throw CommandFailException("The song is not paused.")
        playing?.resume()
    }

    override fun stop() {
        if (playing == null) throw CommandFailException("No song to stop.")
        playing?.stop()
        playing = null
        while(queue.elements().hasMoreElements())
            queue.dequeue()
    }
}