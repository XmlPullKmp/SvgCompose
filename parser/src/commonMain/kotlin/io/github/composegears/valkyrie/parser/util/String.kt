package io.github.composegears.valkyrie.parser.util

fun String.removePrefix(prefix: CharSequence): String {
    if (startsWith(prefix, ignoreCase = true)) {
        return substring(prefix.length)
    }
    return this
}

fun String.capitalized(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}
