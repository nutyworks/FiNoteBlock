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

    override fun next(force: Boolean /* = false */): FailMessage? {

        if (force) {
            playing?.stop()
            playing = queue.dequeue()

            playing?.load()
            playing?.play()
        } else {
            if (queue.elements().hasMoreElements()) {
                playing?.stop()
                playing = queue.dequeue()

                playing?.load()
                playing?.play()
            } else {
                return FailMessage("There is no queued song to play.")
            }
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