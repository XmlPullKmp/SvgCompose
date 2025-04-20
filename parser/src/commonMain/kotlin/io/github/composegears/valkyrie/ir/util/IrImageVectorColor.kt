package io.github.composegears.valkyrie.ir.util

import io.github.composegears.valkyrie.ir.IrColor
import io.github.composegears.valkyrie.ir.IrFill
import io.github.composegears.valkyrie.ir.IrImageVector
import io.github.composegears.valkyrie.ir.IrStroke
import io.github.composegears.valkyrie.ir.IrVectorNode

fun IrImageVector.iconColors(): List<IrColor> {
    val colors = mutableListOf<IrColor>()

    nodes.onEach { node ->
        when (node) {
            is IrVectorNode.IrGroup -> visitGroup(node, colors)
            is IrVectorNode.IrPath  -> visitPath(node, colors)
        }
    }

    return colors
}

private fun visitGroup(
    node: IrVectorNode.IrGroup,
    colors: MutableList<IrColor>,
) {
    node.children.forEach {
        when (it) {
            is IrVectorNode.IrGroup -> visitGroup(it, colors) // TODO: Check recursive call
            is IrVectorNode.IrPath  -> visitPath(it, colors)
        }
    }
}

private fun visitPath(
    node: IrVectorNode.IrPath,
    colors: MutableList<IrColor>,
) {
    when (val fill = node.fill) {
        is IrFill.Color          -> colors += fill.irColor
        is IrFill.LinearGradient -> colors += fill.colorStops.map { it.irColor }
        is IrFill.RadialGradient -> colors += fill.colorStops.map { it.irColor }
        null                     -> Unit
    }
    when (val stroke = node.stroke) {
        is IrStroke.Color -> colors += stroke.irColor
        null              -> Unit
    }
}
