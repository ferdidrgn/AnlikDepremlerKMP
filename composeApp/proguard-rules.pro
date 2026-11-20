# Application-specific R8 rules.
#
# Compose, Firebase, Google Play services, Maps, Ktor, Kotlin serialization,
# coroutines, Koin and AndroidX publish their own consumer rules. Keeping their
# complete packages here prevents shrinking, optimization and obfuscation and
# causes Android Vitals to report a low DEX optimization score.

# Preserve generic signatures and annotations used by generated serializers and
# the small number of libraries that inspect annotated members at runtime.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault

# Do not add package-wide -keep rules. If a future minified release reports a
# missing reflected class, keep only that exact class or annotated member.
