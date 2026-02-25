# ─────────────────────────────────────────────────────────────
# DSquare SDK — Consumer ProGuard Rules
# Only protect what R8 can silently break (reflection / serialization).
# Everything else: let the app's R8 shrink freely.
# ─────────────────────────────────────────────────────────────

# ── Network models (Gson reflection) ────────────────────────
# Gson matches JSON keys to field names via reflection.
# Renaming or removing these fields breaks deserialization silently.
-keep class com.dsquares.library.data.network.model.** { *; }

# ── Generic type info for Gson / Retrofit ────────────────────
# Preserves generic types (e.g. BaseResponse<LoginResult>)
-keepattributes Signature