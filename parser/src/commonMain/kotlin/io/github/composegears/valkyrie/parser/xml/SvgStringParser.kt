package io.github.composegears.valkyrie.parser.xml

import com.fleeksoft.io.byteInputStream
import io.github.composegears.valkyrie.ir.*
import io.github.composegears.valkyrie.parser.xml.ext.*
import io.github.xmlpullkmp.XmlPullParser
import io.github.xmlpullkmp.XmlPullParserKmp

internal object SvgStringParser {

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
                    PATH      -> {
                        val path = parsePath(parser)
                        if (currentGroup != null) {
                            currentGroup.paths.add(path)
                        } else {
                            nodes.add(path)
                        }
                    }
                    GROUP     -> {
                        val group = parseGroup(parser, clipPaths)
                        currentGroup = group
                        nodes.add(group)
                    }
                    CLIP_PATH -> {
                        clipPaths += parseClipPath(parser)
                    }
//                    GRADIENT  -> handleGradient(parser, currentGroup, nodes)
//                    ITEM      -> handleItem(parser, currentGroup, nodes)
                }
            }
        }
        parser.next()
    }
    return nodes
}

private fun parsePath(parser: XmlPullParser): IrVectorNode.IrPath {
    val fillColor = parser.valueAsIrColor(FILL_COLOR)
    val strokeColor = parser.valueAsIrColor(STROKE_COLOR)

    return IrVectorNode.IrPath(
        name = parser.valueAsString(NAME).orEmpty(),
        fill = when {
            fillColor != null && !fillColor.isTransparent() -> IrFill.Color(fillColor)
            else                                            -> null
        },
        stroke = when {
            strokeColor != null && !strokeColor.isTransparent() -> IrStroke.Color(strokeColor)
            else                                                -> null
        },
        strokeAlpha = parser.valueAsFloat(STROKE_ALPHA) ?: 1f,
        fillAlpha = parser.valueAsFloat(FILL_ALPHA) ?: 1f,
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

    val transform = parser.valueAsString(TRANSFORM)

    return IrVectorNode.IrGroup(
        name = parser.valueAsString(ID).orEmpty(),
        rotate = parseTransformAttributes(transform, IrTransformType.Rotate) ?: 0f,
        pivotX = parser.valueAsFloat(PIVOT_X) ?: 0f, // TODO: ?
        pivotY = parser.valueAsFloat(PIVOT_Y) ?: 0f, // TODO: ?
        scaleX = parseTransformAttributes(transform, IrTransformType.ScaleX) ?: 1f,
        scaleY = parseTransformAttributes(transform, IrTransformType.ScaleY) ?: 1f,
        translationX = parseTransformAttributes(transform, IrTransformType.TranslateX) ?: 0f,
        translationY = parseTransformAttributes(transform, IrTransformType.TranslateY) ?: 0f,
        paths = mutableListOf(),
        clipPathData = mutableListOf(),
    )
}

private fun parseTransformAttributes(transform: String?, type: IrTransformType): Float? {
    if(transform.isNullOrEmpty()) return null

    return when (type) {
        IrTransformType.Rotate -> transform
            .substringAfter(ROTATE_START)
            .substringBefore(COMMON_END)
            .split(" ")[0].toFloat()
        IrTransformType.ScaleX -> transform
            .substringAfter(SCALE_START)
            .substringBefore(COMMON_END)
            .split(" ")[0].toFloat()
        // TODO: check if gets scaleY properly
        // (if only 1 argument present scaleY should be the same as scaleX)
        IrTransformType.ScaleY -> transform
            .substringAfter(SCALE_START)
            .substringBefore(COMMON_END)
            .split(" ").take(2).lastOrNull()?.toFloat() ?: 0f
        IrTransformType.TranslateX -> transform
            .substringAfter(TRANSLATE_START)
            .substringBefore(COMMON_END)
            .split(" ")[0].toFloat()
        IrTransformType.TranslateY -> transform
            .substringAfter(TRANSLATE_START)
            .substringBefore(COMMON_END)
            .split(" ").getOrNull(1)?.toFloat() ?: 0f
    }
}

private fun parseClipPath(parser: XmlPullParser): Pair<String, MutableList<IrVectorNode>> {
    val clipPathId = parser.valueAsString(ID) ?: return Pair("", mutableListOf())
    val clipNodes = mutableListOf<IrVectorNode>()



    return Pair(clipPathId, clipNodes)
}

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

private fun handleItem(parser: XmlPullParser, currentGroup: IrVectorNode.IrGroup?, nodes: MutableList<IrVectorNode>) {
    val offset = parser.valueAsFloat(OFFSET) ?: 0f
    val color = parser.valueAsIrColor(COLOR) ?: return
    val colorStop = IrFill.ColorStop(offset, color)

    val lastPath = (currentGroup?.paths?.last() ?: nodes.last()) as? IrVectorNode.IrPath
    when (val fill = lastPath?.fill) {
        is IrFill.LinearGradient -> fill.colorStops.add(colorStop)
        is IrFill.RadialGradient -> fill.colorStops.add(colorStop)
        else                     -> {}
    }
}

// SVG tag names
private const val CLIP_PATH = "clip-path"

private const val GROUP = "g"
private const val PATH = "path"

private const val GRADIENT = "gradient"
private const val ITEM = "item"

// SVG Common Attribute names
private const val ID = "id"
private const val TRANSFORM = "transform"

// SVG Root attribute names
private const val WIDTH = "width"
private const val HEIGHT = "height"
private const val VIEW_BOX = "viewBox"

// SVG Path Attribute Names
private const val PATH_DATA = "d"

// SVG Functions
private const val ROTATE_START = "rotate("
private const val TRANSLATE_START = "translate("
private const val SCALE_START = "scale("
private const val URL_START = "url(#"

private const val COMMON_END = ")"

// Group XML attribute names
const val ROTATION = "android:rotation"
const val PIVOT_X = "android:pivotX"
const val PIVOT_Y = "android:pivotY"
const val SCALE_X = "android:scaleX"
const val SCALE_Y = "android:scaleY"
const val TRANSLATE_X = "android:translateX"
const val TRANSLATE_Y = "android:translateY"

// Path XML attribute names
private const val NAME = "android:name"
private const val FILL_ALPHA = "android:fillAlpha"
private const val STROKE_ALPHA = "android:strokeAlpha"
private const val STROKE_WIDTH = "android:strokeWidth"
private const val STROKE_MITER_LIMIT = "android:strokeMiterLimit"
private const val STROKE_COLOR = "android:strokeColor"
private const val FILL_COLOR = "android:fillColor"

// Item XML attribute names
private const val OFFSET = "android:offset"
private const val COLOR = "android:color"

private data class VectorAttributes(
    val width: Float,
    val height: Float,
    val viewportWidth: Float,
    val viewportHeight: Float,
)
