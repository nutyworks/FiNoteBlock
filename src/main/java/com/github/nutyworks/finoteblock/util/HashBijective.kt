package com.github.nutyworks.finoteblock.util

class HashBijective<P, S>() {
    val primaryToSecondary = HashMap<P, S>()
    val secondaryToPrimary = HashMap<S, P>()

    fun getSecondary(element: P): S? {
        return primaryToSecondary[element]
    }

    fun getPrimary(element: S): P? {
        return secondaryToPrimary[element]
    }

    fun set(primary: P, secondary: S) {
        primaryToSecondary[primary] = secondary
        secondaryToPrimary[secondary] = primary
    }
}

fun <P, S> hashBijectiveOf(): HashBijective<P, S> = HashBijective<P, S>()