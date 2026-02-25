# ─────────────────────────────────────────────────────────────
# DSquare SDK — Library ProGuard / R8 Rules
# Applied when the library module itself has minifyEnabled=true
# ─────────────────────────────────────────────────────────────

# ── Public SDK API ───────────────────────────────────────────
-keep class com.dsquares.library.DSquareSDK { public *; }

-keep class com.dsquares.library.ErrorCode { *; }
-keep class com.dsquares.library.LoginResult { *; }
-keep class com.dsquares.library.LoginResult$* { *; }
-keep class com.dsquares.library.CouponsResult { *; }
-keep class com.dsquares.library.CouponsResult$* { *; }

# ── Network models (Gson reflection) ────────────────────────
-keep class com.dsquares.library.data.network.model.** { *; }

# ── Generic type info for Gson / Retrofit ────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses