package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service

interface ClassroomGetListUseCase {

    fun getList(teacherProfile: TeacherProfile): List<Classroom>
}

@Service
class ClassroomGetListUseCaseImpl(
    private val classroomService: ClassroomService,
    private val coTeacherService: CoTeacherService
): ClassroomGetListUseCase {

    override fun getList(teacherProfile: TeacherProfile): List<Classroom> {

        val classrooms = classroomService.listByTeacher(teacherProfile.id).toMutableList()
        val coClassroomIds = coTeacherService.getClassroomIdListByTeacherId(teacherProfile.id)
        val coClassrooms = classroomService.getAllClassroomAsCoTeacher(coClassroomIds)

        classrooms.addAll(coClassrooms)
        return classrooms
    }
}
