@file:Suppress("UnstableApiUsage") /* LittleEndianDataInputStream */

package com.github.nutyworks.finoteblock.util

import com.google.common.io.LittleEndianDataInputStream

object LittleEndianDataInputStreamUtil

fun LittleEndianDataInputStream.readString(): String {
    val length = readInt()

    var str = ""
    for (i in 0 until length)
        str += readByte().toChar()

    return str
}

fun LittleEndianDataInputStream.skipString(repeat: Int = 1) {
    for (i in 0 until repeat)
        skipBytes(readInt())
}

fun LittleEndianDataInputStream.skipShort(repeat: Int = 1) {
    for (i in 0 until repeat)
        skipBytes(2)
}
