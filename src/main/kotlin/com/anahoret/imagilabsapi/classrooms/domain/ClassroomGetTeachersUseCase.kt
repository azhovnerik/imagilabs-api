package com.anahoret.imagilabsapi.classrooms.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import org.springframework.stereotype.Service
import java.util.*

interface ClassroomGetTeachersUseCase {

    fun get(classroomId: UUID, teacherId: UUID): Either<OperationError, List<ClassroomTeacher>>
}

@Service
class ClassroomGetTeachersUseCaseImpl(
    private val classroomService: ClassroomService,
    private val teacherProfileService: TeacherProfileService,
    private val coTeacherService: CoTeacherService
): ClassroomGetTeachersUseCase {

    override fun get(classroomId: UUID, teacherId: UUID): Either<OperationError, List<ClassroomTeacher>> {

        val classroom = classroomService.getById(classroomId)
            ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()

        val classroomOwner = teacherProfileService.getTeacherById(classroom.teacherId)
            ?.let { ClassroomTeacher.mapFromProfile(it, TeacherRole.OWNER, teacherId == it.id) }
            ?: return NotFoundError("OWNER_NOT_FOUND").left()

        val classroomTeachers = coTeacherService.getAllCoTeachersByClassroomId(classroomId)
            .map { ClassroomTeacher.mapFromCoTeacher(it, teacherId == it.teacherId) }
            .toMutableList()

        return (classroomTeachers + classroomOwner).right()
    }
}
