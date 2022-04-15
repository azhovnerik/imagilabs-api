package com.anahoret.imagilabsapi.teachers.export.googlesheets

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import org.springframework.stereotype.Service

interface GoogleSheetApi {
    fun updateSheet(sheetId: String, range: CellRange, values: List<List<String>>)
}

@Service
@OnGoogleSheetsIntegrationEnabled
class GoogleSheetApiImpl(
    private val sheets: Sheets
) : GoogleSheetApi {

    override fun updateSheet(sheetId: String, range: CellRange, values: List<List<String>>) {
        val content = ValueRange().setValues(values)
        sheets.spreadsheets()
            .values()
            .update(sheetId, range.toR1C1Notation(), content)
            .setValueInputOption(ValueInputOption.RAW)
            .execute()
    }

}
