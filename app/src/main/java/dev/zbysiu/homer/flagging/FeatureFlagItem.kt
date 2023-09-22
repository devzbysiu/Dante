package dev.zbysiu.homer.flagging

data class FeatureFlagItem(
    val key: String,
    val displayName: String,
    val value: Boolean
)