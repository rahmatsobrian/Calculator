# Add project specific ProGuard rules here.
-keep class com.siroha.calculator.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
