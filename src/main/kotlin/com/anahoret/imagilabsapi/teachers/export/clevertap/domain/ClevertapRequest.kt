package com.anahoret.imagilabsapi.teachers.export.clevertap.domain

@Suppress("unused")
class ClevertapRequest(
    val identity: String,
    val type: String,
    val evtName: String,
    val evtData: Any? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClevertapRequest

        if (identity != other.identity) return false
        if (type != other.type) return false
        if (evtName != other.evtName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identity.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + evtName.hashCode()
        return result
    }
}
