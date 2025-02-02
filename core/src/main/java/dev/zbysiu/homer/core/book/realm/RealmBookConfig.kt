package dev.zbysiu.homer.core.book.realm

import io.realm.RealmObject

/**
 * Author:  Martin Macheiner
 * Date:    28.08.2016
 */
open class RealmBookConfig(private var lastPrimaryKey: Long = 0) : RealmObject() {

    fun getLastPrimaryKey(): Long {
        val key = lastPrimaryKey
        lastPrimaryKey++
        return key
    }
}
