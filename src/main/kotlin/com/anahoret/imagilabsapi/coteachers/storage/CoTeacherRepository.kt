package com.anahoret.imagilabsapi.coteachers.storage

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CoTeacherRepository: JpaRepository<CoTeacherEntity, UUID>
