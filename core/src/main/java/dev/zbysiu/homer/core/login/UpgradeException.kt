package dev.zbysiu.homer.core.login

class UpgradeException(cause: Exception?) : Exception(cause?.message ?: "Unknown exception cause")