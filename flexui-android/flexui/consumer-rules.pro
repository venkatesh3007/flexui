# Consumer proguard rules for FlexUI SDK
# These rules will be applied to apps that use FlexUI as a dependency

# Keep FlexUI public API
-keep public class dev.flexui.FlexUI {
    public static *;
}

-keep public class dev.flexui.FlexConfig {
    public *;
}

-keep public class dev.flexui.FlexConfig$Builder {
    public *;
}

# Keep interfaces that apps implement
-keep public interface dev.flexui.FlexComponentFactory {
    public *;
}

-keep public interface dev.flexui.FlexActionHandler {
    public *;
}

-keep public interface dev.flexui.FlexRenderCallback {
    public *;
}

# Keep schema classes for JSON parsing
-keep class dev.flexui.schema.** {
    *;
}

# Keep error handling
-keep public class dev.flexui.FlexError {
    public *;
}

-keep public enum dev.flexui.FlexError$ErrorCode {
    *;
}

# Keep cache policy enum
-keep public enum dev.flexui.CachePolicy {
    *;
}

# Keep companion objects
-keepclassmembers class dev.flexui.** {
    public static final dev.flexui.**$Companion Companion;
}

# Keep @JvmStatic methods
-keepclassmembers class dev.flexui.** {
    @kotlin.jvm.JvmStatic *;
}