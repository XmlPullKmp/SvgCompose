package io.github.composegears.valkyrie.ir

import io.github.composegears.valkyrie.ir.util.circleToPath
import io.github.composegears.valkyrie.ir.util.rectToPath

data class IrImageVector(
    val name: String = "",
    val autoMirror: Boolean = false,
    val defaultWidth: Float,
    val defaultHeight: Float,
    val viewportWidth: Float,
    val viewportHeight: Float,
    val nodes: List<IrVectorNode>,
)

interface IrIdentifiable {
    val name: String?
}

interface IrStylizable {
    val fill: IrFill?
    val fillAlpha: Float
    val fillType: IrFillType
    val stroke: IrStroke?
    val strokeAlpha: Float
    val strokeLineWidth: Float
    val strokeLineCap: IrStrokeLineCap
    val strokeLineJoin: IrStrokeLineJoin
    val strokeLineMiter: Float
}

// CHECK: would be more aligned with IrTransformable
//interface irStylizableRedone {
//    val styles: List<StyleOp>?
//}

// TODO: replace VectorTransform
interface IrTransformable {
    val transforms: List<TransformOp>?
}

sealed interface IrVectorNode {
    data class IrGroup(
        val name: String = "",
        val rotate: Float = 0f,
        val pivotX: Float = 0f,
        val pivotY: Float = 0f,
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
        val translationX: Float = 0f,
        val translationY: Float = 0f,
        val clipPathData: MutableList<IrPathNode>,
        val children: MutableList<IrVectorNode>,
    ) : IrVectorNode

    data class IrPath(
        // IrIdentifiable
        override val name: String? = "",

        // IrStylizable
        override val fill: IrFill? = null,
        override val fillAlpha: Float = 1f,
        override val fillType: IrFillType = IrFillType.NonZero,
        override val stroke: IrStroke? = null,
        override val strokeAlpha: Float = 1f,
        override val strokeLineWidth: Float = 0f,
        override val strokeLineCap: IrStrokeLineCap = IrStrokeLineCap.Butt,
        override val strokeLineJoin: IrStrokeLineJoin = IrStrokeLineJoin.Miter,
        override val strokeLineMiter: Float = 4f,

        val pathNodes: List<IrPathNode>,
    ) : IrVectorNode, IrIdentifiable, IrStylizable

    /**
     * Interface that stores all basic SVG Shapes and exposes common method toPath()
     */
    sealed interface IrShape : IrVectorNode, IrIdentifiable {
        fun IrShape.toPath(): IrPath

        /**
         * SVG aligned 'rect' shape
         * @see{https://svgwg.org/svg2-draft/shapes.html#RectElement}
         */
        data class IrRect(
            override val name: String? = null,

            val x: Float = 0f,
            val y: Float = 0f,
            val width: Float = 0f,
            val height: Float = 0f,
            val rx: Float = 0f,
            val ry: Float = 0f,
        ) : IrShape {
            override fun IrShape.toPath(): IrPath = rectToPath()
        }

        /**
         * SVG aligned 'circle' shape
         * @see{https://svgwg.org/svg2-draft/shapes.html#CircleElement}
         */
        data class IrCircle(
            override val name: String? = null,

            val cx: Float = 0f,
            val cy: Float = 0f,
            val r: Float = 0f,
        ) : IrShape {
            override fun IrShape.toPath(): IrPath = circleToPath()
        }
    }
}

// FIXME: should be replaced with IrTransformable
data class VectorTransform(
    var pivotX: Float = 0f,
    var pivotY: Float = 0f,
    var rotation: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var translateX: Float = 0f,
    var translateY: Float = 0f,
)

sealed interface TransformOp {
    data class Translate(
        val tx: Float,
        val ty: Float,
    ) : TransformOp

    data class Rotate(
        val theta: Float,
        val cx: Float = 0f,
        val cy: Float = 0f,
    ) : TransformOp

    data class Scale(
        val sx: Float,
        val sy: Float,
    ) : TransformOp

    data class Matrix(
        val a: Float,
        val b: Float,
        val c: Float,
        val d: Float,
        val e: Float,
        val f: Float,
    ) : TransformOp

    // TODO: handle skewX and skewY
}

enum class IrFillType {
    EvenOdd,
    NonZero,
}

enum class IrStrokeLineCap(val svgValue: String) {
    Butt("butt"),
    Round("round"),
    Square( "square"),
}

// TODO: Add support for miter-clip and arcs
// https://svgwg.org/svg2-draft/painting.html#LineJoin
enum class IrStrokeLineJoin(val svgValue: String) {
    Miter("miter"),
    Round("round"),
    Bevel("bevel"),
//    MiterClip("miter-clip"),
//    Arcs("arcs")
}

sealed interface IrFill {
    data class Color(val irColor: IrColor) : IrFill

    data class LinearGradient(
        val startY: Float,
        val startX: Float,
        val endY: Float,
        val endX: Float,
        val colorStops: MutableList<ColorStop> = mutableListOf(),
    ) : IrFill

    data class RadialGradient(
        val radius: Float,
        val centerX: Float,
        val centerY: Float,
        val colorStops: MutableList<ColorStop> = mutableListOf(),
    ) : IrFill

    data class ColorStop(
        val offset: Float,
        val irColor: IrColor,
    )
}

sealed interface IrStroke {
    data class Color(val irColor: IrColor) : IrStroke
}

sealed interface IrPathNode {

    data object Close : IrPathNode
    data class RelativeMoveTo(
        val x: Float,
        val y: Float,
    ) : IrPathNode

    data class MoveTo(
        val x: Float,
        val y: Float,
    ) : IrPathNode

    data class RelativeLineTo(
        val x: Float,
        val y: Float,
    ) : IrPathNode

    data class LineTo(
        val x: Float,
        val y: Float,
    ) : IrPathNode

    data class RelativeHorizontalTo(val x: Float) : IrPathNode
    data class HorizontalTo(val x: Float) : IrPathNode
    data class RelativeVerticalTo(val y: Float) : IrPathNode
    data class VerticalTo(val y: Float) : IrPathNode
    data class RelativeCurveTo(
        val dx1: Float,
        val dy1: Float,
        val dx2: Float,
        val dy2: Float,
        val dx3: Float,
        val dy3: Float,
    ) : IrPathNode

    data class CurveTo(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val x3: Float,
        val y3: Float,
    ) : IrPathNode

    data class RelativeReflectiveCurveTo(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
    ) : IrPathNode

    data class ReflectiveCurveTo(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
    ) : IrPathNode

    data class RelativeQuadTo(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
    ) : IrPathNode

    data class QuadTo(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
    ) : IrPathNode

    data class RelativeReflectiveQuadTo(
        val x: Float,
        val y: Float,
    ) : IrPathNode

    data class ReflectiveQuadTo(
        val x: Float,
        val y: Float,
    ) : IrPathNode

    data class RelativeArcTo(
        val horizontalEllipseRadius: Float,
        val verticalEllipseRadius: Float,
        val theta: Float,
        val isMoreThanHalf: Boolean,
        val isPositiveArc: Boolean,
        val arcStartDx: Float,
        val arcStartDy: Float,
    ) : IrPathNode

    data class ArcTo(
        val horizontalEllipseRadius: Float,
        val verticalEllipseRadius: Float,
        val theta: Float,
        val isMoreThanHalf: Boolean,
        val isPositiveArc: Boolean,
        val arcStartX: Float,
        val arcStartY: Float,
    ) : IrPathNode
}
