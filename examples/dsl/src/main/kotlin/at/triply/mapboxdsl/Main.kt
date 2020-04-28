package at.triply.mapboxdsl

import org.w3c.fetch.Response
import kotlin.browser.window

fun createMap(token: String) {
    val map = map {
        accessToken = token.trim()
        container = "map"
        style = "mapbox://styles/mapbox/light-v9"
        bearing = -20.0
        pitch = 65.0
        zoom = 10.4
        center = point(48.19093305465441, 16.411462459889663)

        layers {
            fillExtrusionLayer {
                id = "population"
                geoJsonSource {
                    dataUrl = "/data/vienna_population.geojson"
                }
                paint {
                    color = interpolateLinear(
                        40 * sqrt(get("tot_p")),
                        0 to "#ecda9a",
                        5600 to "#ee4d5a"
                    )
                    height = 40 * sqrt(get("tot_p"))
                    opacity = l(.5)
                }
            }
        }
    }
    map.show()
}

fun main() {
    window.fetch("/MapboxToken").then(Response::text).then(::createMap)
}
