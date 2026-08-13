package com.rodrigos01.aiaudiobook.ui

import kotlin.time.Duration


fun Duration.toDurationString() = toComponents { hours, minutes, seconds, _ ->
    StringBuilder().apply {
        if (hours > 0) {
            append("${hours}:")
        }
        append("$minutes:${seconds.toString().padStart(2, '0')}")
    }.toString()
}