package com.github.nutyworks.finoteblock.noteblock.instrument

import org.bukkit.Sound

class InstrumentManager(val instrument: IInstrument) {

    val customInstruments: HashMap<Byte, CustomInstrument> = HashMap(

    )

    fun register(id: Byte, instrument: CustomInstrument): Boolean {
        if (customInstruments.containsKey(id)) return false
        customInstruments[id] = instrument

        return true
    }

    fun unregisterAll() {
        customInstruments.clear()
    }
}