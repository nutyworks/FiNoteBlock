package com.github.nutyworks.finoteblock.noteblock.instrument

class CustomInstrument(override var soundStr: String, override val pitch: Byte = 45) : IInstrument {
    override fun toString(): String {
        return "CI(soundStr=$soundStr, pitch=$pitch)"
    }
}