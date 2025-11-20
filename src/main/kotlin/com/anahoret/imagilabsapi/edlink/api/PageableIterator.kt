package com.anahoret.imagilabsapi.edlink.api

import arrow.core.Either
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkResponseList

class PageableIterator<T>(private val pageGetter: (cursor: String?) -> Either<OperationError, EdLinkResponseList<T>>) :
    Iterator<List<T>> {
    private lateinit var results: EdLinkResponseList<T>
    private var cursor: String? = null

    override fun next(): List<T> {
        when (val res = pageGetter(cursor)) {
            is Either.Left -> throw RuntimeException("Error getting page")
            is Either.Right -> results = res.value
        }
        if (results.data.isEmpty()) throw NoSuchElementException()
        cursor = results.cursor
        return results.data
    }

    override fun hasNext(): Boolean {
        if (!this::results.isInitialized) {
            return when (val res = pageGetter(cursor)) {
                is Either.Left -> throw RuntimeException("Error getting page")
                is Either.Right -> res.value.data.isNotEmpty()
            }
        }
        return results.cursor != null
    }

}
