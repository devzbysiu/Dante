package dev.zbysiu.tracking.event

import dev.zbysiu.tracking.properties.BaseProperty

data class TrackingProperty(
    private val key: String,
    private val tVal: Any
) : BaseProperty<Any>(tVal) {
    override fun getKey(): String = key
}