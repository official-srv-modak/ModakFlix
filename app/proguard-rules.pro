# ExoPlayer rules
-keep class com.google.android.exoplayer2.** { *; }
-keep class com.google.android.exoplayer2.ui.** { *; }

# Glide rules
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# General App rules
-keep class com.souravmodak.modakflix.** { *; }
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod
