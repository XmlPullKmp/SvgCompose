package io.github.composegears.valkyrie.parser

import io.github.composegears.valkyrie.ir.IrImageVector
import io.github.composegears.valkyrie.parser.svg.SvgToXmlParser
import io.github.composegears.valkyrie.parser.util.IconType
import io.github.composegears.valkyrie.parser.util.IconType.SVG
import io.github.composegears.valkyrie.parser.util.IconType.XML
import io.github.composegears.valkyrie.parser.xml.XmlStringParser
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

data class IconParserOutput(
    val iconType: IconType,
    val irImageVector: IrImageVector,
    val iconName: String,
)

object SvgXmlParser {

    @Throws(IllegalStateException::class)
    fun toIrImageVector(path: Path): IconParserOutput {
        val iconType = IconType.from(path) ?: error("$path must be an SVG or XML file.")

        val fileName = IconNameFormatter.format(name = path.name)
        val text = when (iconType) {
            SVG -> {
                val tmpPath = createTempFile(suffix = "valkyrie/")
                SvgToXmlParser.parse(path, tmpPath)
                tmpPath.readText()
            }
            XML -> path.readText()
        }

        return IconParserOutput(
            iconType = iconType,
            irImageVector = io.github.composegears.valkyrie.parser.xml.XmlStringParser.parse(text),
            iconName = fileName,
        )
    }

    @Throws(IllegalStateException::class)
    fun toIrImageVector(
        value: String,
        iconName: String,
    ): IconParserOutput {
        val iconType = IconType.from(value) ?: error("Unsupported icon type")

        val text = when (iconType) {
            SVG -> {
                val tmpInPath = createTempFile().apply { writeText(value) }
                val tmpOutPath = createTempFile()

                SvgToXmlParser.parse(tmpInPath, tmpOutPath)
                tmpOutPath.readText()
            }
            XML -> value
        }

        return IconParserOutput(
            irImageVector = io.github.composegears.valkyrie.parser.xml.XmlStringParser.parse(text),
            iconName = iconName,
            iconType = iconType,
        )
    }
}
