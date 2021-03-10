# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-dontskipnonpubliclibraryclassmembers
#-dontshrink
#-dontoptimize
#-dontusemixedcaseclassnames
#-keepattributes InnerClasses,EnclosingMethod
#-dontpreverify
-verbose
#-dontwarn android.**, org.json.**

-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }

-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# Keep - Libraries. Keep all public and protected classes, fields, and methods.
-keep public class * {
    public protected <fields>;
    public protected <methods>;
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

## Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
## EnclosingMethod is required to use InnerClasses.
## Already keeped.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

## Retain apiService method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

## Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

## Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

## Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

## Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions

# Model using by retrofit2.
-keep class com.covid19.search.Covid19InfoRequest { *; }
-keep class com.covid19.search.Covid19Info { *; }
-keep class com.covid19.search.Covid19InfoResponse { *; }

# kakaoMap
-keep class net.daum.** {*;}
-keep class android.opengl.** {*;}
-keep class com.kakao.util.maps.helper.** {*;}
-keepattributes Signature
-keepclassmembers class * {
    public static <fields>;
    public *;
}

