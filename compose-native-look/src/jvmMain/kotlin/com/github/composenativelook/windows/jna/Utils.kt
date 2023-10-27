package com.github.composenativelook.windows.jna

internal inline fun <T> Iterable<T>.orOf(selector: (T) -> Int): Int {
    var result = 0
    forEach { result = result or selector(it) }
    return result
}

internal const val INT_SIZE = 4