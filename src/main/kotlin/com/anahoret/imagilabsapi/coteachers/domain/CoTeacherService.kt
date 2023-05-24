package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherEntity
import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface CoTeacherService {

    fun createCoTeacher(classroomId: UUID, teacherEmail: String): CoTeacher
    fun setTeacherIdByEmail(invitationId: UUID, teacherId: UUID)
    fun getCoTeacher(invitationId: UUID): CoTeacher?
    fun getClassroomIdListByTeacherId(teacherId: UUID): List<UUID>
    fun isCoClassroom(classroomId: UUID, teacherId: UUID): Boolean
}

@Service
class CoTeacherServiceImpl(
    private val coTeacherRepository: CoTeacherRepository,
) : CoTeacherService {

    override fun createCoTeacher(
        classroomId: UUID,
        teacherEmail: String,
    ): CoTeacher {
        return coTeacherRepository.save(CoTeacherEntity(classroomId, teacherEmail))
            .let(CoTeacher.Companion::mapFromEntity)

    }

    override fun getCoTeacher(invitationId: UUID): CoTeacher? {
        return coTeacherRepository.findByIdOrNull(invitationId)
            ?.let(CoTeacher.Companion::mapFromEntity)
    }

    override fun setTeacherIdByEmail(invitationId: UUID, teacherId: UUID) {
        coTeacherRepository.findByIdOrNull(invitationId)
            ?.let {
                it.teacherId = teacherId
                coTeacherRepository.save(it)
            }
    }

    override fun getClassroomIdListByTeacherId(teacherId: UUID): List<UUID> {
        return coTeacherRepository.getClassroomIdsByTeacherId(teacherId)
    }

    override fun isCoClassroom(classroomId: UUID, teacherId: UUID): Boolean {
        return coTeacherRepository.existsByClassroomIdAndTeacherId(classroomId, teacherId)
    }
}
