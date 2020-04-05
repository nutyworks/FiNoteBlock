package com.github.nutyworks.finoteblock.noteblock

data class Note(val layer: Short, val instrumentId: Byte, val key: Byte, val volume: Byte = 100, val panning: Byte = 100) {
    override fun toString(): String {
        return "(L$layer,I$instrumentId,K$key,V$volume,P$panning)"
    }
}