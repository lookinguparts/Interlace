**BY DOWNLOADING OR USING THE LX SOFTWARE OR ANY PART THEREOF, YOU AGREE TO THE TERMS AND CONDITIONS OF THE [CHROMATIK / LX SOFTWARE LICENSE AND DISTRIBUTION AGREEMENT](http://chromatik.co/license/).**

Please note that LX is not open-source software. The license grants permission to use this software freely in non-commercial applications. Commercial use is subject to a total annual revenue limit of $25K on any and all projects associated with the software. If this licensing is obstructive to your needs or you are unclear as to whether your desired use case is compliant, contact me to discuss proprietary licensing: licensing@chromatik.co

---

## LX Package

This is a template repository used to demonstrate how to build a package for [Chromatik](https://chromatik.co/) using the [LX](https://github.com/heronarts/LX/) framework.

### Package Structure

- Metadata
  - Define your packages metadata in the [`lx.package`](src/main/resources/lx.package) file, JSON format with three key fields
     - `name`: Name of the package
     - `author`: Name of the package author
     - `mediaDir`: Subfolder name for package static resources in the `~/Chromatik/` user folder
- Java Components
  - Your package can contain custom patterns, effects, modulators, and plugins
  - Java source code lives under [`src/main/java`](src/main/java) and may use the [LX](https://github.com/heronarts/LX/) API
  - Chromatik will automatically import all public non-abstract classes
- Static Resources
  - Static resources like fixture definitions (`*.lxf`), model definitions (`*.lxm`), or project files (`*.lxp`) can be bundled with a package. These live in the [`src/main/resources`](src/main/resources) folder under the subfolders `fixtures`, `models`, `projects`.
  - When the package is imported using the Chromatik UI, these static resources will be copied into the `~/Chromatik` user folder, in a sub-folder defined by the `mediaDir` property of the [`lx.package`](src/main/resources/lx.package) file.

### Building and Installation

Packages are distributed as a JAR file containing all of the above copmonents.

- Build with `mvn package`
- Install via `mvn install`

_Note that `mvn install` does **not** automatically copy static files from [`src/main/resources`](src/main/resources) into your root `~/Chromatik` folder. You can either perform this step manually, or by importing the package using the Chromatik UI._
