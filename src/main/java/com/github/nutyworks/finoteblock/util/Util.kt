package com.github.nutyworks.finoteblock.util

fun Array<out String>.join(separator: String = " ", begin: Int = 0, end: Int = size): String {
    var str = ""

    for (i in begin until end) {
        str += (if (i == begin) "" else separator) + this[i]
    }

    return str
}