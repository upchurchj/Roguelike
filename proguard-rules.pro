# Keep all custom views
-keep public class com.knightlight.game.** extends android.view.View {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep all classes in package (safest for game)
-keep class com.knightlight.game.** { *; }

# Keep enum types
-keepclassmembers enum com.knightlight.game.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep animation callbacks
-keep class * implements android.animation.Animator$AnimatorListener
-keep class * implements android.animation.ValueAnimator$AnimatorUpdateListener

# Keep parcelable and serializable
-keep class * implements android.os.Parcelable { *; }
-keep class * implements java.io.Serializable { *; }
-dontwarn java.awt.Color
