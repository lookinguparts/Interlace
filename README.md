Interlace
========

![Interlace](/assets/InterlaceChromatik.png)

Interlace is a family of three hyperboloids. When in motion, hyperboloids seem to curve and bend, yet are only composed of rigid, straight lines. As participants rotate the outer ring, these strange creatures twist; transforming from a linear body into an elegant hourglass figure and back again.

Installation
=============

Visit [Chromatik.co](https://chromatik.co) to download the latest version of Chromatik.

Read the [Chromatik User Guide](https://chromatik.co/guide).

Backup the standard Chromatik application jar.  
* On Windows, replace the Chromatik application JAR file with lib/[glxstudio-interlace-1.0.0-jar-with-dependencies.jar](lib%2Fglxstudio-interlace-1.0.0-jar-with-dependencies.jar)
* On Mac Apple Silicon, replace the Chromatik applicatin JAR with lib/[glxstudio-interlace-1.0.0-jar-with-dependencies-mac.jar](lib%2Fglxstudio-interlace-1.0.0-jar-with-dependencies-mac.jar)
* On Mac Intel, replace the Chromatik application JAR with lib/[glxstudio-interlace-1.0.0-jar-with-dependencies-mac-intel.jar](lib%2Fglxstudio-interlace-1.0.0-jar-with-dependencies-mac-intel.jar)

This JAR file contains the JOGL libraries necessary for shaders to work.  This should be a temporary measure while issues with the Chromatik Plugin importing process are worked out.
On Windows for example, the Chromatik application JAR file is located at C:\Program Files\Chromatik\app\glxstudio-1.0.0-jar-with-dependencies.
These jar files were built with the v1.0.0 release of Chromatik.

For now, the vertex shaders must be installed manually. Copy the contents of the VShader directory in the repository
to ~/Chromatik/VShaders, including the textures/ subdirectory.

Download the [Interlace package JAR](https://github.com/lookinguparts/Interlace/releases) from releases.  Exit Chromatik.  Copy the JAR
file to ~/Chromatik/Packages.  Remove any pre-existing versions of the Interlace JAR that might be in that directory.  Restart Chromatik.  Once you have the package installed, you should be able to import the model.


![Import](/assets/modelimport.gif)

Choose 'Interlace60x20.lxm'.

![Import2](/assets/modelimport2.png)

If your Interlace content package JAR loaded properly, you should now have a
StripSelector pattern available under the Test section.
![StripSelector](/assets/stripselector.png)

Example Project
================
In the Projects/ directory there is an interlace_base.lxp project file that can be
used as a starting point.  It has the model configured with appropriate view tags and the 
20 foot spacing of hyperboloids.  You should copy the file to your ~/Chromatik/Projects directory.
If it is your first time installing the content package, you might also be able to find the
project in the Chromatik project browser under the Interlace directory.  Until the static content reloading
for third-party packages is released, the easiest method is to just copy the files from our
repository directory to your Chromatik directory.

Views
=======
Chromatik supports the concept of views.  Views can be used to provide patterns with a partial sub-model of the project
such that the pattern only renders to that sub-model.  For our setup, the points in each view will be re-normalized so that
they are centered around the origin of the sub-model and not the origin of world-space. In the screenshot below, the views
have been highlighted.  As you can see, each hyperboloid is rendering a separate shader program.  Each of the H1, H2, and H3
channels have the view selector highlighted and configured to point to the corresponding
view instance as configured in the VIEWS panel.
[![Views](/assets/viewconfig.png)](assets/viewconfig.png)


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

Due to some complications with tinyfd and AppleScript on Mac OS the vertex shader extensions
are .vtx for a vertex shader and .vti for files that will be included by vertex shaders.  You
probably want to set up IntelliJ IDEA to recognize these file extensions as GLSL files. You can
do so by going to Settings -> Editor -> File Types and adding the extensions to the GLSL file type.


Modular Audio/Visual Synthesis Guide
====================================
Read the [Modular Audio/Visual Synthesis User Guide](ModularAudio.md) for more information on combined modular audio and visual synthesis.

