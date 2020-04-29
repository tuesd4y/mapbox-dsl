# Mapbox DSL

![GitHub Workflow Status](<https://img.shields.io/github/workflow/status/tuesd4y/mapbox-dsl/Build LaTeX paper and create release?style=for-the-badge>)
[![download link](<https://img.shields.io/badge/download-latest%20version%20(pdf)-green?style=for-the-badge>)](https://github.com/tuesd4y/mapbox-dsl/releases)

The application of geospatial big data to an increasing number of topics presents new opportunities and challenges for cartographic researchers.
Many emerging technologies focus on providing functionality to implement appealing and informative visualisations.
At the same time, most web-based geospatial data visualisation technologies rely on simple configuration files for defining styling.
We propose a type-safe domain-specific language for implementing reusable map visualisations with MapboxGL to improve development efficiency and utilise IDE support.

## Running the Examples

Unfortunately, all visualizations with Mapbox depend on an access token being set up. You can do this by just creating an account at [mapbox](https://account.mapbox.com/) and copy your default public token into the `examples/MapboxToken` file.

Before running the dsl-example code in the examples directory, the kotlin dsl project has to be compiled. This project depends on Java being installed on your machine and can be built by running

```bash
 ./gradlew build
```

in the `examples/dsl` directory.

Afterwards, both the regular js and dsl examples can be viewed by serving the `examples` directory with a web server of your choice. If you don't have any server installed locally, you can install `node` and run

```bash
 npx serve examples
```

to serve the examples with [serve](https://www.npmjs.com/package/serve).
