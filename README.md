# saker.jar

![Build status](https://img.shields.io/azure-devops/build/sakerbuild/e2bc4049-c2f6-497b-8d3c-bed869f837dd/5/master) [![Latest version](https://mirror.nest.saker.build/badges/saker.jar/version.svg)](https://nest.saker.build/package/saker.jar "saker.jar | saker.nest")

Build tasks and supporting classes for the [saker.build system](https://saker.build) for creating Java Archives during build. The tasks use the [saker.zip](https://github.com/sakerbuild/saker.zip) package behind the scenes, but add additional functionality for convenient JAR creation. (E.g. manifest and service declaration, multi-release support, etc...)

See the [documentation](https://saker.build/saker.jar/doc/) for more information.

## Build instructions

The library uses the [saker.build system](https://saker.build) for building. Use the following command to build the project:

```
java -jar path/to/saker.build.jar -bd build compile saker.build
```

## License

The source code for the project is licensed under *GNU General Public License v3.0 only*.

Short identifier: [`GPL-3.0-only`](https://spdx.org/licenses/GPL-3.0-only.html).

Official releases of the project (and parts of it) may be licensed under different terms. See the particular releases for more information.
