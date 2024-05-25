Interlace
========

![Interlace](/assets/Interlace2.jpg)

Interlace is a family of three hyperboloids. When in motion, hyperboloids seem to curve and bend, yet are only composed of rigid, straight lines. As participants rotate the outer ring, these strange creatures twist; transforming from a linear body into an elegant hourglass figure and back again.

Installation
=============

Visit [Chromatik.co](https://chromatik.co) to download the latest version of Chromatik.

Read the [Chromatik User Guide](https://chromatik.co/guide).

There is a Chromatik model in 'Models/InterlaceV1.lxm'.

There is a sample project in 'Projects/interlace3.lxp'.

Download the model and project files from this repository.  Place them in
~/Chromatik/Models and ~/Chromatik/Projects respectively.

Custom Java Patterns
====================
Patterns are typically implemented as Java classes that extend the LXPattern class.
You will need to be able to edit Java source code and build a JAR file.  Maven is used
for building JAR files.  You will need to install a recent Maven version and JDK version 17.
Once the prerequisites are installed, you can build the JAR file by running 'mvn package'.
You can instal the JAR file by running 'mvn install'.  The JAR file will be placed in your
~/Chromatik/Packages folder.  Note Chromatik must not be running when you run 'mvn install' 
because it will have the JAR file open, preventing the copy-over operation.

* Maven: https://maven.apache.org/
* Temurin 17 JDK: https://adoptopenjdk.net/
* IntelliJ IDEA: https://www.jetbrains.com/idea/
* Eclipse IDE: https://www.eclipse.org/
* VSCode IDE: https://code.visualstudio.com/

Custom Javascript Patterns
==========================
Chromatik has support for implementing patterns in Javascript.  I don't have any experience
using the Javascript interface, but you can find an example in ~\Chromatik\Scripts\Examples\pattern.js.
That example calls renderPoint() for each point in the model/submodel so it can be somewhat limiting.


Custom Shader Patterns
======================
We will have support for custom shader patterns soon.  These will be modified vertext shaders that output ARGB values.
If you want to play with shaders in the meantime, it should be somewhat straightforward to convert a ShaderToy shader
to our custom vertex shader model.  Note that there won't be support for multiple input textures etc such as in ShaderToy.



Modular Audio/Visual Synthesis Guide
====================================
Read the [Modular Audio/Visual Synthesis User Guide](ModularAudio.md) for more information on combined modular audio and visual synthesis.

