package io.github.composegears.valkyrie.parser.util

/**
 * A 3x3 matrix used for 2D transformations.
 * | a c e |
 * | b d f |
 * | 0 0 1 |
 * @see {https://developer.mozilla.org/en-US/docs/Web/SVG/Reference/Attribute/transform#matrix}
 */
data class Matrix3x3(
    val a: Float = 1f,
    val c: Float = 0f,
    val e: Float = 0f,
    val b: Float = 0f,
    val d: Float = 1f,
    val f: Float = 0f,
) {
    companion object {
        val Identity = Matrix3x3()
    }
}