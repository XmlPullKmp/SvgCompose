package io.github.composegears.valkyrie.parser.xml

import io.github.composegears.valkyrie.ir.IrFill
import io.github.composegears.valkyrie.ir.IrImageVector
import io.github.composegears.valkyrie.ir.IrStroke
import io.github.composegears.valkyrie.ir.IrVectorNode
import io.github.composegears.valkyrie.parser.xml.ext.dpValueAsFloat
import io.github.composegears.valkyrie.parser.xml.ext.isAtEnd
import io.github.composegears.valkyrie.parser.xml.ext.seekToStartTag
import io.github.composegears.valkyrie.parser.xml.ext.valueAsBoolean
import io.github.composegears.valkyrie.parser.xml.ext.valueAsFillType
import io.github.composegears.valkyrie.parser.xml.ext.valueAsFloat
import io.github.composegears.valkyrie.parser.xml.ext.valueAsIrColor
import io.github.composegears.valkyrie.parser.xml.ext.valueAsPathData
import io.github.composegears.valkyrie.parser.xml.ext.valueAsString
import io.github.composegears.valkyrie.parser.xml.ext.valueAsStrokeCap
import io.github.composegears.valkyrie.parser.xml.ext.valueAsStrokeLineJoin
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.START_TAG
import org.xmlpull.v1.XmlPullParserFactory

internal object XmlStringParser {

    fun parse(text: String): IrImageVector {
        val parser = io.github.composegears.valkyrie.parser.xml.prepareParser(text)

        check(parser.name == "vector") { "The start tag must be <vector>!" }

        val attributes = io.github.composegears.valkyrie.parser.xml.parseVectorAttributes(parser)

        return IrImageVector(
            defaultWidth = attributes.width,
            defaultHeight = attributes.height,
            viewportWidth = attributes.viewportWidth,
            viewportHeight = attributes.viewportHeight,
            autoMirror = attributes.autoMirrored,
            nodes = io.github.composegears.valkyrie.parser.xml.parseNodes(parser),
        )
    }
}

private fun prepareParser(text: String): XmlPullParser {
    return XmlPullParserFactory.newInstance().newPullParser().apply {
        setInput(text.byteInputStream(), null)
        seekToStartTag()
    }
}

private fun parseVectorAttributes(parser: XmlPullParser): io.github.composegears.valkyrie.parser.xml.VectorAttributes {
    return io.github.composegears.valkyrie.parser.xml.VectorAttributes(
        width = parser.dpValueAsFloat(io.github.composegears.valkyrie.parser.xml.WIDTH) ?: 0f,
        height = parser.dpValueAsFloat(io.github.composegears.valkyrie.parser.xml.HEIGHT) ?: 0f,
        viewportWidth = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.VIEWPORT_WIDTH) ?: 0f,
        viewportHeight = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.VIEWPORT_HEIGHT) ?: 0f,
        autoMirrored = parser.valueAsBoolean(io.github.composegears.valkyrie.parser.xml.AUTO_MIRRORED) ?: false,
    )
}

private fun parseNodes(parser: XmlPullParser): List<IrVectorNode> {
    val nodes = mutableListOf<IrVectorNode>()
    var currentGroup: IrVectorNode.IrGroup? = null

    parser.next()

    while (!parser.isAtEnd()) {
        when (parser.eventType) {
            START_TAG -> {
                when (parser.name) {
                    io.github.composegears.valkyrie.parser.xml.PATH      -> {
                        val path = io.github.composegears.valkyrie.parser.xml.parsePath(parser)
                        if (currentGroup != null) {
                            currentGroup.paths.add(path)
                        } else {
                            nodes.add(path)
                        }
                    }
                    io.github.composegears.valkyrie.parser.xml.GROUP     -> {
                        val group = io.github.composegears.valkyrie.parser.xml.parseGroup(parser)
                        currentGroup = group
                        nodes.add(group)
                    }
                    io.github.composegears.valkyrie.parser.xml.CLIP_PATH -> {
                        currentGroup?.clipPathData?.addAll(parser.valueAsPathData())
                    }
                    io.github.composegears.valkyrie.parser.xml.GRADIENT  -> io.github.composegears.valkyrie.parser.xml.handleGradient(parser, currentGroup, nodes)
                    io.github.composegears.valkyrie.parser.xml.ITEM      -> io.github.composegears.valkyrie.parser.xml.handleItem(parser, currentGroup, nodes)
                }
            }
        }
        parser.next()
    }
    return nodes
}

private fun parsePath(parser: XmlPullParser): IrVectorNode.IrPath {
    val fillColor = parser.valueAsIrColor(io.github.composegears.valkyrie.parser.xml.FILL_COLOR)
    val strokeColor = parser.valueAsIrColor(io.github.composegears.valkyrie.parser.xml.STROKE_COLOR)

    return IrVectorNode.IrPath(
        name = parser.valueAsString(io.github.composegears.valkyrie.parser.xml.NAME).orEmpty(),
        fill = when {
            fillColor != null && !fillColor.isTransparent() -> IrFill.Color(fillColor)
            else -> null
        },
        stroke = when {
            strokeColor != null && !strokeColor.isTransparent() -> IrStroke.Color(strokeColor)
            else -> null
        },
        strokeAlpha = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.STROKE_ALPHA) ?: 1f,
        fillAlpha = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.FILL_ALPHA) ?: 1f,
        strokeLineWidth = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.STROKE_WIDTH) ?: 0f,
        strokeLineCap = parser.valueAsStrokeCap(),
        strokeLineJoin = parser.valueAsStrokeLineJoin(),
        strokeLineMiter = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.STROKE_MITER_LIMIT) ?: 4f,
        pathFillType = parser.valueAsFillType(),
        paths = parser.valueAsPathData(),
    )
}

private fun parseGroup(parser: XmlPullParser): IrVectorNode.IrGroup {
    return IrVectorNode.IrGroup(
        name = parser.valueAsString(io.github.composegears.valkyrie.parser.xml.NAME).orEmpty(),
        rotate = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.ROTATION) ?: 0f,
        pivotX = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.PIVOT_X) ?: 0f,
        pivotY = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.PIVOT_Y) ?: 0f,
        scaleX = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.SCALE_X) ?: 1f,
        scaleY = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.SCALE_Y) ?: 1f,
        translationX = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.TRANSLATE_X) ?: 0f,
        translationY = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.TRANSLATE_Y) ?: 0f,
        paths = mutableListOf(),
        clipPathData = mutableListOf(),
    )
}

private fun handleGradient(
    parser: XmlPullParser,
    currentGroup: IrVectorNode.IrGroup?,
    nodes: MutableList<IrVectorNode>,
) {
    val gradient = io.github.composegears.valkyrie.parser.xml.parseGradient(parser) ?: return

    val lastPath = currentGroup?.paths?.removeLastOrNull() ?: nodes.removeLastOrNull() ?: return
    if (lastPath is IrVectorNode.IrPath && lastPath.fill == null) {
        val gradientPath = lastPath.copy(fill = gradient)
        if (currentGroup != null) {
            currentGroup.paths.add(gradientPath)
        } else {
            nodes.add(gradientPath)
        }
    }
}

private fun parseGradient(parser: XmlPullParser): IrFill? {
    return when (parser.valueAsString(io.github.composegears.valkyrie.parser.xml.TYPE)) {
        io.github.composegears.valkyrie.parser.xml.LINEAR -> {
            val startX = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.START_X) ?: 0f
            val startY = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.START_Y) ?: 0f
            val endX = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.END_X) ?: 0f
            val endY = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.END_Y) ?: 0f

            IrFill.LinearGradient(
                startY = startY,
                startX = startX,
                endY = endY,
                endX = endX,
            )
        }
        io.github.composegears.valkyrie.parser.xml.RADIAL -> {
            val radius = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.GRADIENT_RADIUS) ?: 0f
            val centerX = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.CENTER_X) ?: 0f
            val centerY = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.CENTER_Y) ?: 0f

            IrFill.RadialGradient(
                radius = radius,
                centerX = centerX,
                centerY = centerY,
            )
        }
        else -> null
    }
}

private fun handleItem(parser: XmlPullParser, currentGroup: IrVectorNode.IrGroup?, nodes: MutableList<IrVectorNode>) {
    val offset = parser.valueAsFloat(io.github.composegears.valkyrie.parser.xml.OFFSET) ?: 0f
    val color = parser.valueAsIrColor(io.github.composegears.valkyrie.parser.xml.COLOR) ?: return
    val colorStop = IrFill.ColorStop(offset, color)

    val lastPath = (currentGroup?.paths?.last() ?: nodes.last()) as? IrVectorNode.IrPath
    when (val fill = lastPath?.fill) {
        is IrFill.LinearGradient -> fill.colorStops.add(colorStop)
        is IrFill.RadialGradient -> fill.colorStops.add(colorStop)
        else                     -> {}
    }
}

// XML tag names
private const val CLIP_PATH = "clip-path"
private const val GROUP = "group"
private const val PATH = "path"
private const val GRADIENT = "gradient"
private const val ITEM = "item"

// XML  names
private const val LINEAR = "linear"
private const val RADIAL = "radial"

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

// Gradient XML attribute names
private const val TYPE = "android:type"
private const val START_Y = "android:startY"
private const val START_X = "android:startX"
private const val END_Y = "android:endY"
private const val END_X = "android:endX"
private const val GRADIENT_RADIUS = "android:gradientRadius"
private const val CENTER_X = "android:centerX"
private const val CENTER_Y = "android:centerY"

// Item XML attribute names
private const val OFFSET = "android:offset"
private const val COLOR = "android:color"

// Vector XML attribute names
private const val WIDTH = "android:width"
private const val HEIGHT = "android:height"
private const val VIEWPORT_WIDTH = "android:viewportWidth"
private const val VIEWPORT_HEIGHT = "android:viewportHeight"
private const val AUTO_MIRRORED = "android:autoMirrored"

private data class VectorAttributes(
    val width: Float,
    val height: Float,
    val viewportWidth: Float,
    val viewportHeight: Float,
    val autoMirrored: Boolean,
)
