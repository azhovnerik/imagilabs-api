package com.anahoret.imagilabsapi.common.domain.data

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class UnpagedSorted(private val pageable: Pageable) : Pageable {

    override fun isPaged(): Boolean {
        return false
    }

    override fun getPageNumber(): Int {
        throw UnsupportedOperationException()
    }

    override fun getPageSize(): Int {
        throw UnsupportedOperationException()
    }

    override fun getOffset(): Long {
        throw UnsupportedOperationException()
    }

    override fun getSort(): Sort {
        return pageable.sort
    }

    override fun next(): Pageable {
        return this
    }

    override fun previousOrFirst(): Pageable {
        return this
    }

    override fun first(): Pageable {
        return this
    }

    override fun withPage(pageNumber: Int): Pageable {
        return if (pageNumber == 0) this
        else throw UnsupportedOperationException()
    }

    override fun hasPrevious(): Boolean {
        return false
    }
}
