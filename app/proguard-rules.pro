# ===============================
# VectorCam App ProGuard Rules
# Comprehensive production-ready configuration
# ===============================

# ===============================
# Essential Attributes
# ===============================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses

# ===============================
# Kotlin/Coroutines (Minimal)
# ===============================
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keep class kotlin.coroutines.intrinsics.** { *; }

# ===============================
# Kotlinx Serialization (CRITICAL)
# ===============================
# Keep ALL @Serializable classes with their original names
-keep @kotlinx.serialization.Serializable class * {
    <fields>;
    <methods>;
}

# Keep auto-generated serializers
-keep class **.*$Serializer {
    <fields>;
    <methods>;
}

# Keep serialization framework
-keep class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.json.** { *; }

# ===============================
# Custom Serializers (CRITICAL)
# ===============================
# These are referenced by fully qualified name in @Serializable(with = ...)
-keep class com.vci.vectorcamapp.core.data.dto.serializers.UuidSerializer {
    <fields>;
    <methods>;
}
-keep class com.vci.vectorcamapp.core.data.dto.serializers.SessionTypeSerializer {
    <fields>;
    <methods>;
}

# ===============================
# DataStore Serializers (CRITICAL)
# ===============================
# These implement Serializer<T> interface and are referenced in DI
-keep class com.vci.vectorcamapp.core.data.cache.serializers.** {
    <fields>;
    <methods>;
}

# ===============================
# Types Used in Custom Serializers
# ===============================
# UUID is serialized with custom serializer
-keep class java.util.UUID {
    *;
}

# SessionType enum is serialized with custom serializer
-keep enum com.vci.vectorcamapp.core.domain.model.enums.SessionType {
    *;
}

# ===============================
# Room Database (Minimal)
# ===============================
-keep @androidx.room.Entity class * {
    <fields>;
}
-keep @androidx.room.Dao class * {
    <methods>;
}
-keep @androidx.room.Database class * {
    <methods>;
}

# Room type converters (used via reflection)
-keep @androidx.room.TypeConverter class * {
    <methods>;
}
-keep class com.vci.vectorcamapp.core.data.room.converters.** {
    <methods>;
}

# ===============================
# Hilt/Dagger (DI Framework)
# ===============================
# Keep Hilt entry points and factories
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * {
    <init>(...);
}
-keep @androidx.hilt.work.HiltWorker class * {
    <init>(...);
}

# Keep generated Hilt classes
-keep class **_HiltModules { *; }
-keep class **_Factory { *; }
-keep class **_AssistedFactory { *; }
-keep class dagger.hilt.android.** { *; }

# ===============================
# WorkManager (Background Tasks)
# ===============================
-keep class * extends androidx.work.CoroutineWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ===============================
# Native Libraries (Full Preservation)
# ===============================
# TensorFlow Lite (ML models)
-keep class org.tensorflow.lite.** { *; }
-keepclassmembers class org.tensorflow.lite.** {
    native <methods>;
}

# OpenCV (Image processing)
-keep class org.opencv.** { *; }

# ===============================
# Network Libraries
# ===============================
# Ktor client and serialization
-keep class io.ktor.client.** { *; }
-keep class io.ktor.serialization.kotlinx.json.** { *; }

# ===============================
# TUS Upload Client (Comprehensive)
# ===============================
# Core TUS client classes used in code
-keep class io.tus.java.client.TusClient {
    <fields>;
    <methods>;
}
-keep class io.tus.java.client.TusUpload {
    <init>(...);
    <fields>;
    <methods>;
}
-keep class io.tus.java.client.TusUploader {
    <fields>;
    <methods>;
}
-keep class io.tus.java.client.ProtocolException {
    <init>(...);
    <methods>;
}

# TUS Android-specific classes
-keep class io.tus.android.client.TusPreferencesURLStore {
    <init>(...);
    <methods>;
}

# Custom TUS client implementation
-keep class com.vci.vectorcamapp.core.data.upload.image.util.TimeoutConfiguredTusClient {
    <init>(...);
    <methods>;
}

# HTTP URL Connection (used by TUS for network operations)
-keep class java.net.HttpURLConnection {
    <fields>;
    <methods>;
}

# URL class (used for TUS upload URLs)
-keep class java.net.URL {
    <init>(...);
    <methods>;
}

# SharedPreferences (used by TusPreferencesURLStore)
-keep interface android.content.SharedPreferences {
    <methods>;
}
-keep interface android.content.SharedPreferences$Editor {
    <methods>;
}

# File I/O classes (used for upload preparation)
-keep class java.io.File {
    <init>(...);
    <methods>;
}
-keep class java.io.FileInputStream {
    <init>(...);
    <methods>;
}
-keep class java.io.FileOutputStream {
    <init>(...);
    <methods>;
}
-keep class java.io.InputStream {
    <methods>;
}
-keep class java.io.OutputStream {
    <methods>;
}

# Network exception classes (used in TUS error handling)
-keep class java.net.SocketTimeoutException {
    <init>(...);
    <methods>;
}
-keep class java.io.IOException {
    <init>(...);
    <methods>;
}

# Fix Ktor's Java Management API references (don't exist on Android)
-dontwarn java.lang.management.**
-dontwarn io.ktor.util.debug.**

# ===============================
# Analytics
# ===============================
-keep class com.posthog.android.** { *; }

# ===============================
# BuildConfig (App Constants)
# ===============================
-keep class com.vci.vectorcamapp.BuildConfig {
    public static final java.lang.String POSTHOG_API_KEY;
    public static final java.lang.String POSTHOG_HOST;
    public static final java.lang.String BASE_URL;
}

# ===============================
# Navigation (Compose)
# ===============================
# Only keep @Serializable navigation destinations
-keep @kotlinx.serialization.Serializable class com.vci.vectorcamapp.navigation.** { *; }

# ===============================
# Generic Enum Serialization Support
# ===============================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public java.lang.String name();
}

# ===============================
# Reflection Support (TUS & Others)
# ===============================
# Keep classes with native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep classes that might be accessed via reflection
-keepclassmembers class * {
    @javax.inject.* *;
    @dagger.* *;
}

# Keep exception classes (for proper error handling)
-keep class * extends java.lang.Exception {
    <init>(...);
    public java.lang.String getMessage();
}

# ===============================
# WHAT GETS OBFUSCATED (Good!)
# ===============================
# ViewModels (except Hilt constructors)
# Repositories (except interfaces used in DI)
# Domain models (except those marked @Serializable)
# Presentation layer code
# Business logic
# Utility classes
# Mappers
# Most of your proprietary code!
