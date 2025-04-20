package io.github.composegears.valkyrie.parser.util.ext

import io.github.composegears.valkyrie.ir.VectorTransform
import io.github.composegears.valkyrie.parser.util.Matrix3x3
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.math.atan2

private fun toRadians(theta: Float) = theta * PI / 180f

private fun toDegrees(theta: Float) = theta * 180f / PI

operator fun Matrix3x3.times(other: Matrix3x3): Matrix3x3 = Matrix3x3(
    a = this.a * other.a + this.c * other.b,
    b = this.b * other.a + this.d * other.b,
    c = this.a * other.c + this.c * other.d,
    d = this.b * other.c + this.d * other.d,
    e = this.a * other.e + this.c * other.f + this.e,
    f = this.b * other.e + this.d * other.f + this.f,
)

fun Matrix3x3.preTranslate(tx: Float, ty: Float): Matrix3x3 {
    val translateMatrix = Matrix3x3(e = tx, f = ty)
    return translateMatrix * this
}

fun Matrix3x3.preRotate(theta: Float, cx: Float = 0f, cy: Float = 0f): Matrix3x3 {
    val rad = toRadians(theta)
    val cos = cos(rad).toFloat()
    val sin = sin(rad).toFloat()
    return this
        .preTranslate(-cx, -cy)
        .times(Matrix3x3(a = cos, c = -sin, b = sin, d = cos))
        .preTranslate(cx, cy)
}

fun Matrix3x3.preScale(sx: Float, sy: Float): Matrix3x3 {
    val scaleMatrix = Matrix3x3(a = sx, d = sy)
    return scaleMatrix * this
}

fun Matrix3x3.decomposeToVectorTransform(): VectorTransform {
    val result = VectorTransform()

    result.translateX = this.e
    result.translateY = this.f

    result.scaleX = sqrt(this.a * this.a + this.b * this.b)
    result.scaleY = sqrt(this.c * this.c + this.d * this.d)

    result.rotation = toDegrees(atan2(this.b, this.a)).toFloat()

    return result
}