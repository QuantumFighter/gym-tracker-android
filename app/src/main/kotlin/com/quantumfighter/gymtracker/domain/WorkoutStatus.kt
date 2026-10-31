package com.quantumfighter.gymtracker.domain

/** Explicit completion survives warm-down; unfinished work is neutral, never a failure. */
internal fun exerciseStatus(complete: Boolean, hasSets: Boolean, ending: Boolean): String = when {
    complete -> "COMPLETE"
    hasSets -> "PARTIAL"
    ending -> "SKIPPED"
    else -> "PENDING"
}
