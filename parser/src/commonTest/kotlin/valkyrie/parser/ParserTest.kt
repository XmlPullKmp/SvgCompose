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

    val svg2composeLogo = """
        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <!-- Created with Inkscape (http://www.inkscape.org/) -->

        <svg
           width="66.68364mm"
           height="66.683655mm"
           viewBox="0 0 66.68364 66.683655"
           version="1.1"
           id="svg1"
           xml:space="preserve"
           xmlns="http://www.w3.org/2000/svg"
           xmlns:svg="http://www.w3.org/2000/svg"><defs
             id="defs1" /><g
             id="layer1"
             transform="translate(-127.17607,-58.618422)"><g
               id="g14"><rect
                 style="fill:#222222;fill-opacity:1;stroke-width:0.108967"
                 id="rect1-8"
                 width="66.683647"
                 height="66.683647"
                 x="127.17607"
                 y="58.618423" /><g
                 id="g2"><g
                   id="g3"
                   style="fill:#ffb13b;fill-opacity:1"><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8"
                     cx="160.60208"
                     cy="115.62291"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-2"
                     cx="180.44583"
                     cy="104.24583"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-6"
                     cx="180.44583"
                     cy="81.227081"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-1"
                     cx="160.60208"
                     cy="69.585411"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-9"
                     cx="160.60208"
                     cy="79.90416"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-23"
                     cx="149.48958"
                     cy="86.254158"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-3"
                     cx="149.48958"
                     cy="99.218742"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-77"
                     cx="160.60208"
                     cy="105.56875"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-8"
                     cx="171.71458"
                     cy="98.954163"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-86"
                     cx="171.71458"
                     cy="86.254158"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-65"
                     cx="140.49374"
                     cy="81.227081"
                     r="2.1166666" /><circle
                     style="fill:#ffb13b;fill-opacity:1;stroke:none;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     id="path8-7"
                     cx="140.49374"
                     cy="104.24583"
                     r="2.1166666" /></g><g
                   id="g4"
                   style="stroke:#ffb13b;stroke-opacity:1"><path
                     id="path7"
                     style="fill:none;stroke:#ffb13b;stroke-width:1.32292;stroke-opacity:1"
                     d="m 180.4547,81.14817 v 23.02106 l -19.93682,11.51053 -19.93682,-11.51053 V 81.148171 l 19.93682,-11.510529 z" /><path
                     id="path7-2"
                     style="fill:none;stroke:#ffb13b;stroke-width:2.36234;stroke-dasharray:none;stroke-opacity:1"
                     transform="matrix(0.56000305,0,0,0.56000305,70.627378,40.769545)"
                     d="m 180.4547,81.14817 v 23.02106 l -19.93682,11.51053 -19.93682,-11.51053 V 81.148171 l 19.93682,-11.510529 z" /><path
                     style="fill:#ffb13b;fill-opacity:1;stroke:#ffb13b;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     d="m 160.51788,105.55056 v 10.1292"
                     id="path9" /><path
                     style="fill:#ffb13b;fill-opacity:1;stroke:#ffb13b;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     d="m 171.68256,86.212766 8.77214,-5.064598"
                     id="path10" /><path
                     style="fill:#ffb13b;fill-opacity:1;stroke:#ffb13b;stroke-width:1.32292;stroke-dasharray:none;stroke-opacity:1"
                     d="M 149.3532,86.212766 140.58106,81.14817"
                     id="path11" /></g></g></g></g></svg>
    """.trimIndent()

    @Test
    fun shouldParseSvgToIrImageVector() {
        val ir = SvgStringParser.parse(svg2composeLogo)
        ir.nodes.forEach(::println)
    }

}