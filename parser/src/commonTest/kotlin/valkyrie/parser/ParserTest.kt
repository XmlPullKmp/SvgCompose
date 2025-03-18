package valkyrie.parser

import io.github.composegears.valkyrie.parser.xml.SvgStringParser
import kotlin.test.Test

class ParserTest {

    val dummySvg = """
        <?xml version="1.0" encoding="UTF-8"?>
        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="24px" height="24px" viewBox="0 0 24 24" version="1.1">
        <defs>
        <linearGradient id="linear0" gradientUnits="userSpaceOnUse" x1="0.26599" y1="1.465591" x2="0.738447" y2="0.523814" gradientTransform="matrix(24,0,0,12.039063,0,11.960938)">
        <stop offset="0.09677" style="stop-color:rgb(0%,58.431373%,83.529412%);stop-opacity:1;"/>
        <stop offset="0.3007" style="stop-color:rgb(13.72549%,54.117647%,85.098039%);stop-opacity:1;"/>
        <stop offset="0.6211" style="stop-color:rgb(33.333333%,48.235294%,87.058824%);stop-opacity:1;"/>
        <stop offset="0.8643" style="stop-color:rgb(45.490196%,44.705882%,88.627451%);stop-opacity:1;"/>
        <stop offset="1" style="stop-color:rgb(50.196078%,43.137255%,89.019608%);stop-opacity:1;"/>
        </linearGradient>
        <linearGradient id="linear1" gradientUnits="userSpaceOnUse" x1="0.139841" y1="0.371049" x2="0.686824" y2="-0.135538" gradientTransform="matrix(12.039062,0,0,13,0,0)">
        <stop offset="0.1183" style="stop-color:rgb(0%,58.431373%,83.529412%);stop-opacity:1;"/>
        <stop offset="0.4178" style="stop-color:rgb(23.529412%,51.372549%,86.27451%);stop-opacity:1;"/>
        <stop offset="0.6962" style="stop-color:rgb(42.745098%,45.490196%,88.235294%);stop-opacity:1;"/>
        <stop offset="0.8333" style="stop-color:rgb(50.196078%,43.137255%,89.019608%);stop-opacity:1;"/>
        </linearGradient>
        <linearGradient id="linear2" gradientUnits="userSpaceOnUse" x1="-0.168362" y1="0.919397" x2="0.762192" y2="-0.0111567" gradientTransform="matrix(24,0,0,24,0,0)">
        <stop offset="0.1075" style="stop-color:rgb(78.039216%,34.117647%,73.72549%);stop-opacity:1;"/>
        <stop offset="0.2138" style="stop-color:rgb(81.568627%,37.647059%,60.392157%);stop-opacity:1;"/>
        <stop offset="0.4254" style="stop-color:rgb(88.235294%,44.705882%,36.078431%);stop-opacity:1;"/>
        <stop offset="0.6048" style="stop-color:rgb(93.333333%,49.411765%,18.431373%);stop-opacity:1;"/>
        <stop offset="0.743" style="stop-color:rgb(96.078431%,52.54902%,7.45098%);stop-opacity:1;"/>
        <stop offset="0.8232" style="stop-color:rgb(97.254902%,53.72549%,3.529412%);stop-opacity:1;"/>
        </linearGradient>
        </defs>
        <g id="surface1">
        <path style=" stroke:none;fill-rule:nonzero;fill:url(#linear0);" d="M 0 24 L 12.039062 11.960938 L 24 24 Z M 0 24 "/>
        <path style=" stroke:none;fill-rule:nonzero;fill:url(#linear1);" d="M 0 0 L 12.039062 0 L 0 13 Z M 0 0 "/>
        <path style=" stroke:none;fill-rule:nonzero;fill:url(#linear2);" d="M 12.039062 0 L 0 12.679688 L 0 24 L 24 0 Z M 12.039062 0 "/>
        </g>
        </svg>
    """.trimIndent()

    val dummyXml = """
        <vector xmlns:android="http://schemas.android.com/apk/res/android" xmlns:aapt="http://schemas.android.com/aapt"
            android:viewportWidth="24"
            android:viewportHeight="24"
            android:width="24dp"
            android:height="24dp">
            <path
                android:pathData="M0 24L12.039062 11.960938L24 24ZM0 24">
                <aapt:attr
                    name="android:fillColor">
                    <gradient
                        android:startX="6.38376"
                        android:startY="29.60528"
                        android:endX="17.72273"
                        android:endY="18.26717"
                        android:tileMode="clamp">
                        <item
                            android:color="#0095D5"
                            android:offset="0.09677" />
                        <item
                            android:color="#238AD9"
                            android:offset="0.3007" />
                        <item
                            android:color="#557BDE"
                            android:offset="0.6211" />
                        <item
                            android:color="#7472E2"
                            android:offset="0.8643" />
                        <item
                            android:color="#806EE3"
                            android:offset="1" />
                    </gradient>
                </aapt:attr>
            </path>
            <path
                android:pathData="M0 0L12.039062 0L0 13ZM0 0">
                <aapt:attr
                    name="android:fillColor">
                    <gradient
                        android:startX="1.683554"
                        android:startY="4.823637"
                        android:endX="8.268717"
                        android:endY="-1.761994"
                        android:tileMode="clamp">
                        <item
                            android:color="#0095D5"
                            android:offset="0.1183" />
                        <item
                            android:color="#3C83DC"
                            android:offset="0.4178" />
                        <item
                            android:color="#6D74E1"
                            android:offset="0.6962" />
                        <item
                            android:color="#806EE3"
                            android:offset="0.8333" />
                    </gradient>
                </aapt:attr>
            </path>
            <path
                android:pathData="M12.039062 0L0 12.679688L0 24L24 0ZM12.039062 0">
                <aapt:attr
                    name="android:fillColor">
                    <gradient
                        android:startX="-4.040688"
                        android:startY="22.06553"
                        android:endX="18.29261"
                        android:endY="-0.2677608"
                        android:tileMode="clamp">
                        <item
                            android:color="#C757BC"
                            android:offset="0.1075" />
                        <item
                            android:color="#D0609A"
                            android:offset="0.2138" />
                        <item
                            android:color="#E1725C"
                            android:offset="0.4254" />
                        <item
                            android:color="#EE7E2F"
                            android:offset="0.6048" />
                        <item
                            android:color="#F58613"
                            android:offset="0.743" />
                        <item
                            android:color="#F88909"
                            android:offset="0.8232" />
                    </gradient>
                </aapt:attr>
            </path>
        </vector>
    """.trimIndent()

    @Test
    fun shouldParseSvgToIrImageVector() {
        val ir = SvgStringParser.parse(dummySvg)
        ir.nodes.forEach(::println)
    }

}