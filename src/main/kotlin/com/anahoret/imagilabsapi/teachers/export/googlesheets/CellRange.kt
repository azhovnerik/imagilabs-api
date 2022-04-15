package com.anahoret.imagilabsapi.teachers.export.googlesheets

class CellRange(
    private val sheetName: String,
    private val startRow: Int,
    private val startCol: Int,
    private val endRow: Int,
    private val endCol: Int
) {

    fun toR1C1Notation(): String {
        return "$sheetName!R${startRow}C${startCol}:R${endRow}C${endCol}"
    }
}
