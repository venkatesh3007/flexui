# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep the sample app's main activity
-keep class dev.flexui.sample.MainActivity {
    *;
}

# FlexUI rules are automatically included via consumer-rules.pro