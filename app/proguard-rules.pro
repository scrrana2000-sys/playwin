# ProGuard & R8 Optimization Rules for Release Build

# Keep app data and model classes
-keep class com.myplaywin.app.data.** { *; }
-keepclassmembers class com.myplaywin.app.data.** { *; }

# Firebase Realtime Database & Auth
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
    @com.google.firebase.database.IgnoreExtraProperties <fields>;
}

# Google Play Services & AdMob
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# Moshi & Retrofit
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# Preserve line numbers for crash reports
-keepattributes SourceFile,LineNumberTable

