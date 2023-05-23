package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherEntity
import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherRepository
import org.springframework.stereotype.Service
import java.util.*

interface InvitationCoTeacherService {

    fun createCoTeacher(classroomId: UUID, teacherEmail: String): CoTeacher
}

@Service
class InvitationCoTeacherServiceImpl(
    private val coTeacherRepository: CoTeacherRepository
) : InvitationCoTeacherService {

    override fun createCoTeacher(
        classroomId: UUID,
        teacherEmail: String,
    ): CoTeacher {
        return coTeacherRepository.save(CoTeacherEntity(classroomId, teacherEmail))
            .let(CoTeacher.Companion::mapFromEntity)

    }
}
