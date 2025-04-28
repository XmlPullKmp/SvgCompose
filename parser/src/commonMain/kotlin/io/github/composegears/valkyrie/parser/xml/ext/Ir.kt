package io.github.composegears.valkyrie.parser.xml.ext

import io.github.composegears.valkyrie.ir.IrColor
import io.github.composegears.valkyrie.ir.IrFillType
import io.github.composegears.valkyrie.ir.IrPathNode
import io.github.composegears.valkyrie.ir.IrStrokeLineCap
import io.github.composegears.valkyrie.ir.IrStrokeLineJoin
import io.github.composegears.valkyrie.parser.xml.PathParser
import io.github.xmlpullkmp.XmlPullParser

internal fun XmlPullParser.valueAsPathData(): List<IrPathNode> {
    return PathParser.parsePathString(getAttribute(PATH_DATA).orEmpty())
}

internal fun XmlPullParser.valueAsFillType(): IrFillType = stringAsFillType(getAttribute(FILL_TYPE)) ?: IrFillType.NonZero

internal fun stringAsFillType(value: String?): IrFillType? {
    return when (value) {
        EVENODD -> IrFillType.EvenOdd
        NONZERO -> IrFillType.NonZero
        else    -> null
    }
}

internal fun XmlPullParser.valueAsStrokeCap(): IrStrokeLineCap {
    val value = getAttribute(STROKE_LINE_CAP)
    return stringAsStrokeCap(value) ?: IrStrokeLineCap.Butt
}

internal fun stringAsStrokeCap(value: String?): IrStrokeLineCap? {
    return IrStrokeLineCap.entries
        .find { it.svgValue.equals(value, ignoreCase = true) }
}

internal fun XmlPullParser.valueAsStrokeLineJoin(): IrStrokeLineJoin {
    val value = getAttribute(STROKE_LINE_JOIN)
    return stringAsStrokeLineJoin(value) ?: IrStrokeLineJoin.Miter
}

internal fun stringAsStrokeLineJoin(value: String?): IrStrokeLineJoin? {
    return IrStrokeLineJoin.entries
        .find { it.svgValue.equals(value, ignoreCase = true) }
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