package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.lovable.domain.accountcards.StudentsLovableAccountCardsFormat
import java.util.*

class DownloadStudentsLovableAccountsRequest(
    val format: StudentsLovableAccountCardsFormat,
    val studentIds: Set<UUID>?
)
