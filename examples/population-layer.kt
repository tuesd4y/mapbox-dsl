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