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


# Obfuscate all classes and methods by default
#-obfuscate

-dontwarn dalvik.system.VMRuntime

-if class org.lsposed.hiddenapibypass.HiddenApiBypass
-keepclassmembers class org.lsposed.hiddenapibypass.Helper$* { *; }

-assumenosideeffects class android.util.Property{
    public static *** of(...);
}

# Specific rules for obfuscating methods or fields (can be customized as needed)
-keepclassmembers class * {
    public void *();  # Keep all public methods
}

-keepclassmembers class * {
    public void verifySdkActivation();
}

# Keep native methods from obfuscation
#-keep class com.nexa.awesome.core.system.api.MetaActivationManager { *; }
-keepclasseswithmembers class * {
    native <methods>;
}

-keep class com.nexa.awesome.** {*; }
-keep class top.niunaijun.jnihook.** {*; }

-keep class black.** {*; }
-keep class android.** {*; }
-keep class com.android.** {*; }

-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Keep encryption classes and methods
-keep class android.encrypt.** { *; }
-keepclassmembers class android.encrypt.** { 
    public *; 
}

# Keep annotations
-keep @interface android.encrypt.StringEncrypt

# Keep methods used for decryption
-keepclassmembers class * {
    @android.encrypt.StringEncrypt *;
}

# Prevent obfuscation of decryption methods
-keepclassmembers class * {
    public static *** decrypt(...);
    public static *** deobfuscate(...);
}

-keep class top.niunaijun.blackreflection.** {*; }
-keep @top.niunaijun.blackreflection.annotation.BClass class * {*;}
-keep @top.niunaijun.blackreflection.annotation.BClassName class * {*;}
-keep @top.niunaijun.blackreflection.annotation.BClassNameNotProcess class * {*;}
-keepclasseswithmembernames class * {
    @top.niunaijun.blackreflection.annotation.BField.* <methods>;
    @top.niunaijun.blackreflection.annotation.BFieldNotProcess.* <methods>;
    @top.niunaijun.blackreflection.annotation.BFieldSetNotProcess.* <methods>;
    @top.niunaijun.blackreflection.annotation.BFieldCheckNotProcess.* <methods>;
    @top.niunaijun.blackreflection.annotation.BMethod.* <methods>;
    @top.niunaijun.blackreflection.annotation.BStaticField.* <methods>;
    @top.niunaijun.blackreflection.annotation.BStaticMethod.* <methods>;
    @top.niunaijun.blackreflection.annotation.BMethodCheckNotProcess.* <methods>;
    @top.niunaijun.blackreflection.annotation.BConstructor.* <methods>;
    @top.niunaijun.blackreflection.annotation.BConstructorNotProcess.* <methods>;
}