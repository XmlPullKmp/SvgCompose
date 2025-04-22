package io.github.composegears.valkyrie.ir.util

import io.github.composegears.valkyrie.ir.IrPathNode
import io.github.composegears.valkyrie.ir.IrVectorNode.*

fun IrShape.IrRect.rectToPath(): IrPath {
    val isRounded = rx > 0f && ry > 0f

    return IrPath(
        name = this.name,
        pathNodes = listOfNotNull(
            IrPathNode.MoveTo(x + rx, y),
            IrPathNode.HorizontalTo(x + width - rx),
            IrPathNode.ArcTo(
                horizontalEllipseRadius = rx,
                verticalEllipseRadius = ry,
                theta = 0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                arcStartX = x + width,
                arcStartY = y + ry
            ).takeIf { isRounded },
            IrPathNode.VerticalTo(y + height - ry),
            IrPathNode.ArcTo(
                horizontalEllipseRadius = rx,
                verticalEllipseRadius = ry,
                theta = 0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                arcStartX = x + width - rx,
                arcStartY = y + height
            ).takeIf { isRounded },
            IrPathNode.HorizontalTo(x + rx),
            IrPathNode.ArcTo(
                horizontalEllipseRadius = rx,
                verticalEllipseRadius = ry,
                theta = 0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                arcStartX = x,
                arcStartY = y + height - ry
            ).takeIf { isRounded },
            IrPathNode.VerticalTo(y + ry),
            IrPathNode.ArcTo(
                horizontalEllipseRadius = rx,
                verticalEllipseRadius = ry,
                theta = 0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                arcStartX = x + rx,
                arcStartY = y
            ).takeIf { isRounded },
            IrPathNode.Close
        )
    )
}

fun IrShape.IrCircle.circleToPath(): IrPath {
    return IrPath(
        name = name,
        pathNodes = listOf(
            IrPathNode.MoveTo(cx + r, cy),
            IrPathNode.ArcTo(
                horizontalEllipseRadius = r,
                verticalEllipseRadius = r,
                theta = 0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                arcStartX = cx,
                arcStartY = cy + r
            ),
            IrPathNode.ArcTo(
                horizontalEllipseRadius = r,
                verticalEllipseRadius = r,
                theta = 0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                arcStartX = cx - r,
                arcStartY = cy
            ),
            IrPathNode.ArcTo(
                horizontalEllipseRadius = r,
                verticalEllipseRadius = r,
                theta = 0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                arcStartX = cx,
                arcStartY = cy - r
            ),
            IrPathNode.ArcTo(
                horizontalEllipseRadius = r,
                verticalEllipseRadius = r,
                theta = 0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                arcStartX = cx + r,
                arcStartY = cy
            ),
            IrPathNode.Close
        )
    )
}