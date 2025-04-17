package io.github.composegears.valkyrie.parser.util

data class Matrix3x3(
    val a: Float = 1f,
    val b: Float = 0f,
    val c: Float = 0f,
    val d: Float = 0f,
    val e: Float = 1f,
    val f: Float = 0f,
) {
    companion object {
        val Identity = Matrix3x3()
    }
}