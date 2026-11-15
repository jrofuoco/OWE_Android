# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class xyz.iamdua.owe.**$$serializer { *; }
-keepclassmembers class xyz.iamdua.owe.** {
    *** Companion;
}
-keepclasseswithmembers class xyz.iamdua.owe.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor & Coroutines
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**
-dontwarn org.slf4j.**
