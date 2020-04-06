package com.github.nutyworks.finoteblock.command

class PlayerOnlyCommandException(override val message: String? = "This command is for player.") : Exception()