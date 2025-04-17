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
    a = this.a * other.a + this.b * other.d,
    b = this.a * other.b + this.b * other.e,
    c = this.a * other.c + this.b * other.f + this.c,
    d = this.d * other.a + this.e * other.d,
    e = this.d * other.b + this.e * other.e,
    f = this.d * other.c + this.e * other.f + this.f,
)

fun Matrix3x3.preTranslate(tx: Float, ty: Float): Matrix3x3 {
    val translateMatrix = Matrix3x3(c = tx, f = ty)
    return translateMatrix * this
}

fun Matrix3x3.preRotate(theta: Float, cx: Float = 0f, cy: Float = 0f): Matrix3x3 {
    val rad = toRadians(theta)
    val cos = cos(rad).toFloat()
    val sin = sin(rad).toFloat()
    return this
        .preTranslate(-cx, -cy)
        .times(Matrix3x3(a = cos, b = -sin, c = sin, d = cos))
        .preTranslate(cx, cy)
}

fun Matrix3x3.preScale(sx: Float, sy: Float): Matrix3x3 {
    val scaleMatrix = Matrix3x3(a = sx, e = sy)
    return scaleMatrix * this
}

fun Matrix3x3.decomposeToVectorTransform(): VectorTransform {
    val result = VectorTransform()

    result.translateX = this.c
    result.translateY = this.f

    result.scaleX = sqrt(this.a * this.a + this.d * this.d)
    result.scaleY = sqrt(this.b * this.b + this.e * this.e)

    result.rotation = toDegrees(atan2(this.d, this.a)).toFloat()

    return result
}