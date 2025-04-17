package io.github.composegears.valkyrie.parser.xml

import com.fleeksoft.io.byteInputStream
import io.github.composegears.valkyrie.ir.*
import io.github.composegears.valkyrie.ir.TransformOp.*
import io.github.composegears.valkyrie.parser.util.Matrix3x3
import io.github.composegears.valkyrie.parser.util.ext.decomposeToVectorTransform
import io.github.composegears.valkyrie.parser.util.ext.preRotate
import io.github.composegears.valkyrie.parser.util.ext.preScale
import io.github.composegears.valkyrie.parser.util.ext.preTranslate
import io.github.composegears.valkyrie.parser.xml.ext.*
import io.github.xmlpullkmp.XmlPullParser
import io.github.xmlpullkmp.XmlPullParserKmp

object SvgStringParser {

    fun parse(text: String): IrImageVector {
        val parser = prepareParser(text)

        check(parser.getName() == "svg") { "The start tag must be <svg>!" }

        val attributes = parseSVGAttributes(parser)

        return IrImageVector(
            defaultWidth = attributes.width,
            defaultHeight = attributes.height,
            viewportWidth = attributes.viewportWidth,
            viewportHeight = attributes.viewportHeight,
            nodes = parseSVGNodes(parser),
        )
    }
}

private fun prepareParser(text: String): XmlPullParser {
    return XmlPullParserKmp().apply {
        setInput(text.byteInputStream(), null)
        seekToStartTag()
    }
}

private fun parseSVGAttributes(parser: XmlPullParser): VectorAttributes {
    val (viewportWidth, viewportHeight) = parser.valueAsString(VIEW_BOX)?.let { viewBox ->
        viewBox.split("[ ,]+".toRegex())
            .takeIf { it.size >= 4 }
            ?.mapNotNull { it.toFloatOrNull() }
            ?.let { Pair(it[2], it[3]) }
    } ?: Pair(0f, 0f)

    return VectorAttributes(
        width = parser.dpValueAsFloat(WIDTH) ?: 0f,
        height = parser.dpValueAsFloat(HEIGHT) ?: 0f,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
    )
}

//--- COMPLETE

private fun parseSVGNodes(parser: XmlPullParser): List<IrVectorNode> {
    val nodes = mutableListOf<IrVectorNode>()
    var currentGroup: IrVectorNode.IrGroup? = null

    // TODO: Split IrVectorNode (separate group, add shapes)
    // TODO: ClipPaths stored/accessible as MutableList<IrPathNode>
    val clipPaths = mutableMapOf<String, MutableList<IrVectorNode>>()

    parser.next()

    while (!parser.isAtEnd()) {
        when (parser.getEventType()) {
            XmlPullParser.START_TAG -> {
                when (parser.getName()) {
                    PATH -> {
                        val path = parsePath(parser)
                        if (currentGroup != null) {
                            currentGroup.paths.add(path)
                        } else {
                            nodes.add(path)
                        }
                    }

                    GROUP -> {
                        val group = parseGroup(parser, clipPaths)
                        currentGroup = group
                        nodes.add(group)
                    }

                    CLIP_PATH -> {
                        clipPaths += parseClipPath(parser)
                    }
                }
            }
        }
        parser.next()
    }
    return nodes
}

private fun parsePath(parser: XmlPullParser): IrVectorNode.IrPath {
    val fillColor = parser.valueAsIrColor(FILL)
    val strokeColor = parser.valueAsIrColor(STROKE)

    return IrVectorNode.IrPath(
        name = parser.valueAsString(ID).orEmpty(),
        fill = when {
            fillColor != null && !fillColor.isTransparent() -> IrFill.Color(fillColor)
            else -> null
        },
        stroke = when {
            strokeColor != null && !strokeColor.isTransparent() -> IrStroke.Color(strokeColor)
            else -> null
        },
        strokeAlpha = parser.valueAsFloat(STROKE_OPACITY) ?: 1f,
        fillAlpha = parser.valueAsFloat(FILL_OPACITY) ?: 1f,
        strokeLineWidth = parser.valueAsFloat(STROKE_WIDTH) ?: 0f,
        strokeLineCap = parser.valueAsStrokeCap(),
        strokeLineJoin = parser.valueAsStrokeLineJoin(),
        strokeLineMiter = parser.valueAsFloat(STROKE_MITER_LIMIT) ?: 4f,
        pathFillType = parser.valueAsFillType(),
        paths = parser.valueAsPathData(),
    )
}

private fun parseGroup(parser: XmlPullParser, clipPaths: Map<String, MutableList<IrVectorNode>>): IrVectorNode.IrGroup {
    // TODO: clipPath flattening ?
    val clipPathData = parser.valueAsString(CLIP_PATH)?.let { clipUrl ->
        val clipPathId = clipUrl.substringAfter(URL_START).substringBefore(COMMON_END)
        clipPaths[clipPathId] ?: mutableListOf()
    }

    val vectorTransform = parseTransform(parser.valueAsString(TRANSFORM) ?: "")

    return IrVectorNode.IrGroup(
        name = parser.valueAsString(ID).orEmpty(),
        rotate = vectorTransform.rotation,
        pivotX = vectorTransform.pivotX,
        pivotY = vectorTransform.pivotY,
        scaleX = vectorTransform.scaleX,
        scaleY = vectorTransform.scaleY,
        translationX = vectorTransform.translateX,
        translationY = vectorTransform.translateY,
        paths = mutableListOf(),
        clipPathData = mutableListOf(),
    )
}

private fun parseClipPath(parser: XmlPullParser): Pair<String, MutableList<IrVectorNode>> {
    val clipPathId = parser.valueAsString(ID) ?: return Pair("", mutableListOf())
    val clipNodes = mutableListOf<IrVectorNode>()

    // TODO: clipPath flattening ?

    return Pair(clipPathId, clipNodes)
}

private fun parseTransform(transformString: String): VectorTransform {
    val ops = parseTransformOps(transformString)
    var matrix = Matrix3x3.Identity
    var pivot: Pair<Float, Float>? = null

    // First pass: Look for explicit pivot patterns
    for (i in 0..ops.size - 3) {
        val (t1, op, t2) = ops.slice(i..i+2)
        if (t1 is Translate && t2 is Translate &&
            t1.tx == -t2.tx && t1.ty == -t2.ty
        ) {
            pivot = t1.tx to t1.ty
            when (op) {
                is Rotate -> return VectorTransform(
                    pivotX = pivot.first,
                    pivotY = pivot.second,
                    rotation = op.theta,
                    translateX = matrix.e,
                    translateY = matrix.f
                )
                is Scale -> return VectorTransform(
                    pivotX = pivot.first,
                    pivotY = pivot.second,
                    scaleX = op.sx,
                    scaleY = op.sy,
                    translateX = matrix.e,
                    translateY = matrix.f
                )
                else -> {}
            }
        }
    }

    // Second pass: Apply all operations to matrix
    ops.forEach { op ->
        matrix = when (op) {
            is Translate -> matrix.preTranslate(op.tx, op.ty)
            is Rotate -> matrix.preRotate(op.theta, op.cx, op.cy)
            is Scale -> matrix.preScale(op.sx, op.sy)
        }
    }

    // Decompose final matrix
    return matrix.decomposeToVectorTransform().apply {
        pivot?.let {
            pivotX = it.first
            pivotY = it.second
        }
    }
}

private fun parseTransformOps(transformString: String): List<TransformOp> {
    val regex = Regex("""(\w+)\(([^)]*)\)""")
    return regex.findAll(transformString.replace(",", " ")).map { match ->
        val values = match.groupValues[2].split(' ').filter { it.isNotBlank() }.map { it.toFloat() }
        when (match.groupValues[1]) {
            TRANSLATE -> Translate(values[0], values.getOrElse(1) { 0f })
            ROTATE -> when {
                values.size >= 3 -> Rotate(values[0], values[1], values[2])
                else -> Rotate(values[0])
            }

            SCALE -> Scale(values[0], values.getOrElse(1) { values[0] })
            else -> throw IllegalArgumentException("Unsupported transform: ${match.groupValues[1]}")
        }
    }.toList()
}

//private fun detectPivot(ops: List<TransformOp>): Pair<Float, Float>? {
//    if (ops.size < 3) return null
//    val (preTranslate, _, postTranslate) = ops.take(3)
//
//    return when {
//        preTranslate is Translate &&
//                postTranslate is Translate &&
//                preTranslate.tx == -postTranslate.tx &&
//                preTranslate.ty == -postTranslate.ty -> Pair(preTranslate.tx, preTranslate.ty)
//
//        else -> null
//    }
//}

// TODO: Handle gradients

//private fun handleGradient(
//    parser: XmlPullParser,
//    currentGroup: IrVectorNode.IrGroup?,
//    nodes: MutableList<IrVectorNode>,
//) {
//    val gradient = parseGradient(parser) ?: return
//
//    val lastPath = currentGroup?.paths?.removeLastOrNull() ?: nodes.removeLastOrNull() ?: return
//    if (lastPath is IrVectorNode.IrPath && lastPath.fill == null) {
//        val gradientPath = lastPath.copy(fill = gradient)
//        if (currentGroup != null) {
//            currentGroup.paths.add(gradientPath)
//        } else {
//            nodes.add(gradientPath)
//        }
//    }
//}

//private fun parseGradient(parser: XmlPullParser): IrFill? {
//    return when (parser.valueAsString(TYPE)) {
//        LINEAR -> {
//            val startX = parser.valueAsFloat(START_X) ?: 0f
//            val startY = parser.valueAsFloat(START_Y) ?: 0f
//            val endX = parser.valueAsFloat(END_X) ?: 0f
//            val endY = parser.valueAsFloat(END_Y) ?: 0f
//
//            IrFill.LinearGradient(
//                startY = startY,
//                startX = startX,
//                endY = endY,
//                endX = endX,
//            )
//        }
//        RADIAL -> {
//            val radius = parser.valueAsFloat(GRADIENT_RADIUS) ?: 0f
//            val centerX = parser.valueAsFloat(CENTER_X) ?: 0f
//            val centerY = parser.valueAsFloat(CENTER_Y) ?: 0f
//
//            IrFill.RadialGradient(
//                radius = radius,
//                centerX = centerX,
//                centerY = centerY,
//            )
//        }
//        else                                              -> null
//    }
//}

// TODO: Handle gradients
//private fun handleItem(parser: XmlPullParser, currentGroup: IrVectorNode.IrGroup?, nodes: MutableList<IrVectorNode>) {
//    val offset = parser.valueAsFloat(OFFSET) ?: 0f
//    val color = parser.valueAsIrColor(COLOR) ?: return
//    val colorStop = IrFill.ColorStop(offset, color)
//
//    val lastPath = (currentGroup?.paths?.last() ?: nodes.last()) as? IrVectorNode.IrPath
//    when (val fill = lastPath?.fill) {
//        is IrFill.LinearGradient -> fill.colorStops.add(colorStop)
//        is IrFill.RadialGradient -> fill.colorStops.add(colorStop)
//        else -> {}
//    }
//}

// SVG tag names
private const val CLIP_PATH = "clip-path"
private const val GROUP = "g"
private const val PATH = "path"

// SVG Common Properties names
private const val ID = "id"
private const val TRANSFORM = "transform"

// SVG Root Properties names
private const val WIDTH = "width"
private const val HEIGHT = "height"
private const val VIEW_BOX = "viewBox"

// SVG Transform Properties Names
private const val TRANSLATE = "translate"
private const val ROTATE = "rotate"
private const val SCALE = "scale"

// SVG Path Properties Names
private const val FILL = "fill"
private const val FILL_OPACITY = "fill-opacity"

private const val STROKE = "stroke"
private const val STROKE_OPACITY = "stroke-opacity"
private const val STROKE_WIDTH = "stroke-width"
private const val STROKE_MITER_LIMIT = "stroke-miterlimit"

// SVG Functions // TODO: Review
private const val URL_START = "url(#"
private const val COMMON_END = ")"

private data class VectorAttributes(
    val width: Float,
    val height: Float,
    val viewportWidth: Float,
    val viewportHeight: Float,
)
