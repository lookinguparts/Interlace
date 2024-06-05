Interlace
========

![Interlace](/assets/Interlace2.jpg)

Interlace is a family of three hyperboloids. When in motion, hyperboloids seem to curve and bend, yet are only composed of rigid, straight lines. As participants rotate the outer ring, these strange creatures twist; transforming from a linear body into an elegant hourglass figure and back again.

Installation
=============

Visit [Chromatik.co](https://chromatik.co) to download the latest version of Chromatik.

Read the [Chromatik User Guide](https://chromatik.co/guide).

Backup the standard Chromatik application jar.  Replace the Chromatik application JAR file with lib/[glxstudio-interlace-1.0.0-jar-with-dependencies.jar](lib%2Fglxstudio-interlace-1.0.0-jar-with-dependencies.jar)
This JAR file contains the JOGL libraries necessary for shaders to work.  This should be a temporary measure while issues with the Chromatik Plugin importing process are worked out.
On Windows for example, the Chromatik application JAR file is located at C:\Program Files\Chromatik\app\glxstudio-1.0.0-jar-with-dependencies.

Download the [Interlace package JAR](https://github.com/lookinguparts/Interlace/releases) from releases.  Copy the JAR
file to ~/Chromatik/Packages.  Remove any pre-existing versions of the Interlace JAR that might be in that directory.  Once you have the package installed, you should be able to import the model.



![Import](/assets/modelimport.gif)

Choose 'InterlaceV1.lxm'.

![Import2](/assets/modelimport2.png)

If your Interlace content package JAR loaded properly, you should now have a
StripSelector pattern available under the Test section.
![StripSelector](/assets/stripselector.png)

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
There is support for custom shader patterns.  These are modified vertex shaders that output RGB values.
After you install the package, you should create the directory ~/Chromatik/VShaders/ and place the contents of the repository's VShader/ directory in it.  There currently is not
support for textures but that will be coming soon.  In order to render on the surface of a cylinder, you should
see the coordinate space remapping example in ShpRings.vert.  The shaders support an #include <funcs.vert> directive for sharing
common blocks of code.


Modular Audio/Visual Synthesis Guide
====================================
Read the [Modular Audio/Visual Synthesis User Guide](ModularAudio.md) for more information on combined modular audio and visual synthesis.

