package com.github.nutyworks.finoteblock.noteblock.song

enum class Recipient {
    EVERYONE,
    WORLD,
    PLAYER;

    companion object fun valueOf(i: Int) {
        when(i) {
            0 -> EVERYONE
            1 -> WORLD
            2 -> PLAYER
        }
    }
}