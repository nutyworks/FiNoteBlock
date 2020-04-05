package com.github.nutyworks.finoteblock.noteblock

import com.github.nutyworks.finoteblock.noteblock.song.NoteBlockSong

class NoteBlockSongManager() {
    val playing = HashMap<String, NoteBlockSong>()
    private var maxId = 0

    fun register(song: NoteBlockSong): String {
        val key = song.hashCode().toString(36)
        playing[key] = song

        return key
    }

    fun unregister(id: String): NoteBlockSong? {
        return playing.remove(id)
    }

    fun getSong(id: String): NoteBlockSong? {
        return playing[id]
    }
}