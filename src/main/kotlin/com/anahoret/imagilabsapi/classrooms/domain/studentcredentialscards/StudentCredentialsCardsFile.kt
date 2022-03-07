package com.anahoret.imagilabsapi.classrooms.domain.studentcredentialscards

import java.io.InputStream

class StudentCredentialsCardsFile(
    val inputStream: InputStream,
    val fileName: String,
    val format: StudentsCredentialsCardsFormat
)
