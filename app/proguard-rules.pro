# Gretel's own classes are kept whole and unobfuscated. Fragments, the custom
# SnapScrollView inflated from XML, and generated ViewBinding classes all live
# under this package, so nothing in the app is renamed or removed. R8 only
# shrinks the AndroidX and Kotlin libraries, which is where the size goes.
-keep class com.abeant.gretel.** { *; }
