package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.classrooms.domain.studentcredentialscards.StudentsCredentialsCardsFormat
import java.util.*

class DownloadStudentsCredentialsRequest(
    val format: StudentsCredentialsCardsFormat,
    val studentIds: Set<UUID>?
)
