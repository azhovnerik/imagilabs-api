package com.anahoret.imagilabsapi.teachers.export.googlesheets.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ConditionalOnProperty(prefix = "integrations.google.sheets", name = ["enabled"], havingValue = "true")
annotation class OnGoogleSheetsIntegrationEnabled
