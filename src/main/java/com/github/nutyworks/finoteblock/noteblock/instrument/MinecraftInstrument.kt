package com.github.nutyworks.finoteblock.noteblock.instrument

class MinecraftInstrument(override val soundStr: String, override val pitch: Byte = 45) : IInstrument {
    enum class Type(val instrument: MinecraftInstrument) {
        PIANO(MinecraftInstrument("minecraft:block.note_block.harp")),
        DOUBLE_BASS(MinecraftInstrument("minecraft:block.note_block.bass")),
        BASS_DRUM(MinecraftInstrument("minecraft:block.note_block.basedrum")),
        SNARE_DRUM(MinecraftInstrument("minecraft:block.note_block.snare")),
        CLICK(MinecraftInstrument("minecraft:block.note_block.hat")),
        GUITAR(MinecraftInstrument("minecraft:block.note_block.guitar")),
        FLUTE(MinecraftInstrument("minecraft:block.note_block.flute")),
        BELL(MinecraftInstrument("minecraft:block.note_block.bell")),
        CHIME(MinecraftInstrument("minecraft:block.note_block.chime")),
        XYLOPHONE(MinecraftInstrument("minecraft:block.note_block.xylophone")),
        IRON_XYLOPHONE(MinecraftInstrument("minecraft:block.note_block.iron_xylophone")),
        COW_BELL(MinecraftInstrument("minecraft:block.note_block.cow_bell")),
        DIDGERIDOO(MinecraftInstrument("minecraft:block.note_block.didgeridoo")),
        BIT(MinecraftInstrument("minecraft:block.note_block.bit")),
        BANJO(MinecraftInstrument("minecraft:block.note_block.banjo")),
        PLING(MinecraftInstrument("minecraft:block.note_block.pling"));

        companion object {
            val hashMap = HashMap<Byte, String>().apply {
                for (type in values()) {
                    put(type.ordinal.toByte(), type.instrument.soundStr)
                }
            }
        }
    }
}