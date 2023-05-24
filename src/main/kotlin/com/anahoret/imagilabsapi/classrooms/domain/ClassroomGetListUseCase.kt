package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomGetListUseCase {

    fun getList(teacherId: UUID): List<Classroom>
}

@Service
class ClassroomGetListUseCaseImpl(
    private val classroomService: ClassroomService,
    private val coTeacherService: CoTeacherService
): ClassroomGetListUseCase {

    override fun getList(teacherId: UUID): List<Classroom> {

        val classrooms = classroomService.listByTeacher(teacherId).toMutableList()
        val coClassroomIds = coTeacherService.getClassroomIdListByTeacherId(teacherId)
        val coClassrooms = classroomService.getAllClassroomAsCoTeacher(coClassroomIds)

        classrooms.addAll(coClassrooms)
        return classrooms
    }
}
