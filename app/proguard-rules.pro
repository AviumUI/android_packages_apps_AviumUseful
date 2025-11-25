# Add project specific ProGuard rules here.

# Keep all attributes needed by R8
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Signature
-keepattributes Exceptions
-keepattributes SourceFile
-keepattributes LineNumberTable

# Jetpack Compose - Extended rules
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }
-keep @androidx.compose.ui.tooling.preview.Preview class * { *; }
-keep class * implements androidx.compose.runtime.MonotonicFrameClock
-keepclassmembers class androidx.compose.** { *; }

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# AndroidX
-keep class androidx.activity.** { *; }
-keep class androidx.preference.** { *; }
-dontwarn androidx.**

# Keep app classes
-keep class org.exthm.exthmuseful.** { *; }
-keepclassmembers class org.exthm.exthmuseful.** { *; }

# R8 compatibility
-allowaccessmodification
