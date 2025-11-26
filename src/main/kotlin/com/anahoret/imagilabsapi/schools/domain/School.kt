package com.anahoret.imagilabsapi.schools.domain

import com.anahoret.imagilabsapi.schools.storage.SchoolEntity
import java.util.*

data class School(
    val id: UUID,
    val name: String
) {
    companion object {
        fun fromEntity(entity: SchoolEntity): School {
            return School(
                id = entity.id!!,
                name = entity.name
            )
        }
    }
}