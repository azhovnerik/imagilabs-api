package com.anahoret.imagilabsapi.coteachers.domain

import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherEntity
import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface CoTeacherService {

    fun createCoTeacher(classroomId: UUID, teacherEmail: String, teacherId: UUID?): CoTeacher
    fun acceptInvitation(coTeacherId: UUID, teacherId: UUID)
    fun acceptInvitation(coTeacherId: UUID)
    fun getCoTeacher(coTeacherId: UUID): CoTeacher?
    fun getClassroomIdListByTeacherId(teacherId: UUID): List<UUID>
    fun isCoClassroom(classroomId: UUID, teacherId: UUID): Boolean
    fun getAllCoTeachersByClassroomId(classroomId: UUID): List<CoTeacher>
    fun deleteCoTeacher(coTeacherId: UUID)
    fun getByClassroomIdAndTeacherId(classroomId: UUID, teacherId: UUID): CoTeacher?
    fun getCoTeacherCountByClassroomId(classroomId: UUID): Long
    fun getCoTeacherCountsByClassroomIds(classroomIds: Iterable<UUID>): Map<UUID, Long>
}

@Service
class CoTeacherServiceImpl(
    private val coTeacherRepository: CoTeacherRepository,
) : CoTeacherService {

    override fun createCoTeacher(
        classroomId: UUID,
        teacherEmail: String,
        teacherId: UUID?
    ): CoTeacher {
        return coTeacherRepository.save(CoTeacherEntity(classroomId, teacherEmail, teacherId))
            .let { getCoTeacher(it.id!!)!! }
    }

    override fun getCoTeacher(coTeacherId: UUID): CoTeacher? {
        return coTeacherRepository.getCoTeacherById(coTeacherId)
            ?.let(CoTeacher.Companion::mapFromCoTeacherData)
    }

    override fun acceptInvitation(coTeacherId: UUID, teacherId: UUID) {
        coTeacherRepository.findByIdOrNull(coTeacherId)
            ?.let {
                it.teacherId = teacherId
                it.coTeacherStatus = TeacherRole.CO_TEACHER
                coTeacherRepository.save(it)
            }
    }

    override fun acceptInvitation(coTeacherId: UUID) {
        coTeacherRepository.findByIdOrNull(coTeacherId)
            ?.let {
                it.coTeacherStatus = TeacherRole.CO_TEACHER
                coTeacherRepository.save(it)
            }
    }

    override fun getClassroomIdListByTeacherId(teacherId: UUID): List<UUID> {
        return coTeacherRepository.getClassroomIdsByTeacherId(teacherId)
    }

    override fun isCoClassroom(classroomId: UUID, teacherId: UUID): Boolean {
        return coTeacherRepository.existsByClassroomIdAndTeacherId(classroomId, teacherId)
    }

    override fun getAllCoTeachersByClassroomId(classroomId: UUID): List<CoTeacher> {
        return coTeacherRepository.findAllByClassroomId(classroomId)
            .map { CoTeacher.mapFromCoTeacherData(it) }
    }

    override fun getByClassroomIdAndTeacherId(classroomId: UUID, teacherId: UUID): CoTeacher? {
       return coTeacherRepository.findByClassroomIdAndTeacherId(classroomId, teacherId)
           ?.let(CoTeacher.Companion::mapFromCoTeacherData)
    }

    override fun getCoTeacherCountByClassroomId(classroomId: UUID): Long {
        return coTeacherRepository.countAllByClassroomId(classroomId)
    }

    override fun getCoTeacherCountsByClassroomIds(classroomIds: Iterable<UUID>): Map<UUID, Long> {
        return coTeacherRepository.getCoTeacherCountsByClassrooms(classroomIds)
            .associate { it.classroomId to it.coTeacherCount }
    }

    override fun deleteCoTeacher(coTeacherId: UUID) {
        coTeacherRepository.deleteById(coTeacherId)
    }
}
