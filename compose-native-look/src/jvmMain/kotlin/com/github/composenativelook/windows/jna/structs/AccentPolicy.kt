package com.github.composenativelook.windows.jna.structs

import com.github.composenativelook.windows.jna.enums.AccentFlag
import com.github.composenativelook.windows.jna.enums.AccentState
import com.github.composenativelook.windows.jna.orOf
import com.sun.jna.Structure.FieldOrder

@Suppress("unused")
@FieldOrder(
    "accentState",
    "accentFlags",
    "color",
    "animationId",
)
internal class AccentPolicy(
    accentState: AccentState = AccentState.ACCENT_DISABLED,
    accentFlags: Set<AccentFlag> = emptySet(),
    @JvmField var color: Int = 0,
    @JvmField var animationId: Int = 0,
) : BaseStructure() {

    @JvmField
    var accentState: Int = accentState.value

    @JvmField
    var accentFlags: Int = accentFlags.orOf { it.value }
}