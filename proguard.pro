-injars       target\Interlace-0.0.2-SNAPSHOT-jar-with-dependencies.jar
-outjars      target\interlace.jar
-libraryjars  <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-keep class art.lookingup.**
-keepclassmembers public class art.lookingup.** {
    *;
}
-keep class jogamp.nativewindow.windows.GDIUtil
-keepclassmembers public class jogamp.nativewindow.windows.GDIUtil {
    *;
}
-keep class com.jogamp.opengl.GLProfile
-keepclassmembers public class com.jogamp.opengl.GLProfile {
    *;
}
-keep class com.jogamp.nativewindow.AbstractGraphicsDevice
-keepclassmembers public class com.jogamp.nativewindow.AbstractGraphicsDevice {
    *;
}
-keep class com.jogamp.opengl.GL3
-keep class jogamp.opengl.GLDrawableFactoryImpl
-keep class com.jogamp.opengl.util.texture.Texture
-keep class jogamp.opengl.util.pngj.ImageInfo
-keep class com.jogamp.opengl.**
-keepclassmembers class com.jogamp.opengl.** { *; }
-keep class com.jogamp.nativewindow.**
-keepclassmembers class com.jogamp.nativewindow.** { *; }
-keep class com.jogamp.common.**
-keepclassmembers class com.jogamp.common.** { *; }
-keep class jogamp.opengl.**
-keepclassmembers class jogamp.opengl.** { *; }
-keep class jogamp.nativewindow.**
-keepclassmembers class jogamp.nativewindow.** { *; }





-dontoptimize
-dontobfuscate
-dontwarn **
