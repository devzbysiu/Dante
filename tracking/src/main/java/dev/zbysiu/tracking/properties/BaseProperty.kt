package dev.zbysiu.tracking.properties

abstract class BaseProperty<out T>(val value: T) {
    abstract fun getKey(): String
}