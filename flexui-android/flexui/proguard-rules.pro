# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep all public API classes and methods
-keep public class dev.flexui.FlexUI {
    public static *;
}

-keep public class dev.flexui.FlexConfig {
    public *;
}

-keep public class dev.flexui.FlexConfig$Builder {
    public *;
}

# Keep all interfaces that host apps will implement
-keep public interface dev.flexui.FlexComponentFactory {
    public *;
}

-keep public interface dev.flexui.FlexActionHandler {
    public *;
}

-keep public interface dev.flexui.FlexRenderCallback {
    public *;
}

# Keep schema classes used in JSON parsing
-keep class dev.flexui.schema.** {
    public *;
}

# Keep enum values
-keepclassmembers enum dev.flexui.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin companion objects and @JvmStatic methods
-keep class dev.flexui.**$Companion {
    public *;
}

-keepclassmembers class dev.flexui.** {
    @kotlin.jvm.JvmStatic *;
}

# Keep error classes for proper exception handling
-keep public class dev.flexui.FlexError {
    public *;
}

-keep public class dev.flexui.FlexError$ErrorCode {
    public *;
}

# Don't obfuscate JSON field names
-keepclassmembers class dev.flexui.schema.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep View constructors and methods that may be called via reflection
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep custom component factories
-keep class * implements dev.flexui.FlexComponentFactory {
    public *;
}

# Keep action handlers
-keep class * implements dev.flexui.FlexActionHandler {
    public *;
}

# Keep render callbacks
-keep class * implements dev.flexui.FlexRenderCallback {
    public *;
}