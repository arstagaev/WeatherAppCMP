package com.tagaev.utils

private val leadingNoise = Regex("^[\\uFEFF\\u200B\\u200E\\u200F\\u00A0\\s]+")
fun String.cleanJsonStart(): String = replace(leadingNoise, "")