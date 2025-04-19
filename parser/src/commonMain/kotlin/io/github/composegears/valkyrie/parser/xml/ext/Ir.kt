package io.github.composegears.valkyrie.parser.xml.ext

import io.github.composegears.valkyrie.ir.IrColor
import io.github.composegears.valkyrie.ir.IrPathFillType
import io.github.composegears.valkyrie.ir.IrPathNode
import io.github.composegears.valkyrie.ir.IrStrokeLineCap
import io.github.composegears.valkyrie.ir.IrStrokeLineJoin
import io.github.composegears.valkyrie.parser.xml.PathParser
import io.github.xmlpullkmp.XmlPullParser

internal fun XmlPullParser.valueAsPathData(): List<IrPathNode> {
    return PathParser.parsePathString(getAttribute(PATH_DATA).orEmpty())
}

internal fun XmlPullParser.valueAsFillType(): IrPathFillType {
    return when (getAttribute(FILL_TYPE)) {
        EVENODD -> IrPathFillType.EvenOdd
        NONZERO -> IrPathFillType.NonZero
        else    -> IrPathFillType.NonZero
    }
}

internal fun XmlPullParser.valueAsStrokeCap(): IrStrokeLineCap {
    val value = getAttribute(STROKE_LINE_CAP)

    return IrStrokeLineCap.entries
        .find { it.svgValue.equals(value, ignoreCase = true) }
        ?: IrStrokeLineCap.Butt
}

internal fun XmlPullParser.valueAsStrokeLineJoin(): IrStrokeLineJoin {
    val value = getAttribute(STROKE_LINE_JOIN)

    return IrStrokeLineJoin.entries
        .find { it.svgValue.equals(value, ignoreCase = true) }
        ?: IrStrokeLineJoin.Miter
}

internal fun XmlPullParser.valueAsIrColor(name: String): IrColor? {
    return getAttribute(name)?.let { IrColor(it) }
}

internal fun XmlPullParser.valueAsStyle(): Map<String, String> {
    val style = getAttribute(STYLE).orEmpty()
    return style.split(";")
        .mapNotNull { record -> record
            .split(":")
            .takeIf { elements -> elements.size == 2 }
        }
        .associate { (key, value) -> key.trim() to value.trim() }
}

// SVG Path Attribute Names
private const val PATH_DATA = "d"

private const val STYLE = "style"

private const val FILL = "fill"
private const val STROKE = "stroke"
private const val STROKE_WIDTH = "stroke-width"
private const val STROKE_LINE_CAP = "stroke-linecap"
private const val STROKE_LINE_JOIN = "stroke-linejoin"

// SVG Fill type
private const val FILL_TYPE = "fill-rule"
private const val EVENODD = "evenodd"
private const val NONZERO = "nonzero"