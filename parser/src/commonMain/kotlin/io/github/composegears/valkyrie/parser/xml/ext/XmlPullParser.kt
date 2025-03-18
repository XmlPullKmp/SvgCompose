package io.github.composegears.valkyrie.parser.xml.ext

import io.github.xmlpullkmp.XmlPullParser


internal fun XmlPullParser.dpValueAsFloat(name: String): Float? {
    return getAttribute(name)
        ?.removeSuffix("dp")
        ?.toFloatOrNull()
}

internal fun XmlPullParser.valueAsBoolean(name: String): Boolean? = getAttribute(name)?.toBooleanStrictOrNull()

internal fun XmlPullParser.valueAsString(name: String): String? = getAttribute(name)

internal fun XmlPullParser.valueAsFloat(name: String): Float? = getAttribute(name)?.toFloatOrNull()

internal fun XmlPullParser.getAttribute(name: String): String? = getAttributeValue("", name)

internal fun XmlPullParser.seekToStartTag(): XmlPullParser {
    var type = next()

    while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
        // Empty loop
        type = next()
    }

    if (type != XmlPullParser.START_TAG) {
        throw Exception("No start tag found")
    }
    return this
}

internal fun XmlPullParser.isAtEnd() = getEventType() == XmlPullParser.END_DOCUMENT || (getDepth() < 1 && getEventType() == XmlPullParser.END_TAG)
