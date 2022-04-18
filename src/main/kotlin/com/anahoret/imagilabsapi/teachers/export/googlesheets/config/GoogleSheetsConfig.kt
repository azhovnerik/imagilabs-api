package com.anahoret.imagilabsapi.teachers.export.googlesheets.config

import com.anahoret.imagilabsapi.applicationproperties.domain.ApplicationPropertiesKey
import com.anahoret.imagilabsapi.applicationproperties.domain.ApplicationPropertiesService
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream

@Configuration
@OnGoogleSheetsIntegrationEnabled
class GoogleSheetsConfig(
    private val applicationPropertiesService: ApplicationPropertiesService
) {

    @Bean
    fun sheets(): Sheets {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val googleApiKeyFile = applicationPropertiesService.getProperty(ApplicationPropertiesKey.GOOGLE_API_KEY_FILE)
        val credentials = GoogleCredentials
            .fromStream(ByteArrayInputStream(googleApiKeyFile.toByteArray()))
            .createScoped(SheetsScopes.SPREADSHEETS)
        return Sheets.Builder(
            httpTransport,
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        ).build()
    }

}
