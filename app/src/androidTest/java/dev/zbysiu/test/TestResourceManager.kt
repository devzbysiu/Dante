package dev.zbysiu.test

object TestResourceManager {

    fun getTestResourceAsString(clazz: Class<*>, fileName: String): String {
        return clazz.getResourceAsStream(fileName)
            ?.bufferedReader()
            ?.lineSequence()
            ?.joinToString("")
            ?: ""
    }
}