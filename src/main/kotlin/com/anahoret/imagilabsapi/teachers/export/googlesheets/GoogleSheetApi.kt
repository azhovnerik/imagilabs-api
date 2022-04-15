package com.anahoret.imagilabsapi.teachers.export.googlesheets

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import org.springframework.stereotype.Service

interface GoogleSheetApi {

    fun updateSheet(sheetId: String, range: SheetRange, values: List<List<String>>)
    fun getSheet(sheetId: String, range: SheetRange): List<List<String>>
}

@Service
@OnGoogleSheetsIntegrationEnabled
class GoogleSheetApiImpl(
    private val sheets: Sheets
) : GoogleSheetApi {

    override fun updateSheet(sheetId: String, range: SheetRange, values: List<List<String>>) {
        val content = ValueRange().setValues(values)
        sheets.spreadsheets()
            .values()
            .update(sheetId, range.toR1C1Notation(), content)
            .setValueInputOption(ValueInputOption.RAW)
            .execute()
    }

    override fun getSheet(sheetId: String, range: SheetRange): List<List<String>> {
        return sheets.spreadsheets()
            .values()
            .get(sheetId, range.toR1C1Notation())
            .execute()
            .getValues()
            .orEmpty()
            .map { it.map(Any::toString) }
    }

}
