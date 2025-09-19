package com.anahoret.imagilabsapi.lovable.domain.accountcards

import java.io.InputStream

class StudentLovableAccountCardsFile(
    val inputStream: InputStream,
    val fileName: String,
    val format: StudentsLovableAccountCardsFormat
)
