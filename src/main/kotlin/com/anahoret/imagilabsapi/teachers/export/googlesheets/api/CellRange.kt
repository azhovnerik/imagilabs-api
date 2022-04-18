package com.anahoret.imagilabsapi.teachers.export.googlesheets.api

interface SheetRange {

    fun toR1C1Notation(): String
}

class EntireSheetRange(
    private val sheetName: String,
) : SheetRange {

    override fun toR1C1Notation(): String {
        return sheetName
    }
}

class CellRange(
    private val sheetName: String,
    private val startRow: Int,
    private val startCol: Int,
    private val endRow: Int,
    private val endCol: Int
) : SheetRange {

    override fun toR1C1Notation(): String {
        return "$sheetName!R${startRow}C${startCol}:R${endRow}C${endCol}"
    }
}
