package com.anahoret.imagilabsapi.edlink.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomCreateRequest
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.edlink.api.EdLinkClassApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkIntegrationApi
import com.anahoret.imagilabsapi.students.domain.StudentCreateRequest
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import org.springframework.stereotype.Service
import java.util.*

interface EdLinkRefreshTeacherClassesUseCase {
    fun refresh(teacherProfile: TeacherProfile): Either<OperationError, Unit>
}

@Service
class EdLinkRefreshTeacherClassesUseCaseImpl(
    private val edLinkIntegrationApi: EdLinkIntegrationApi,
    private val edLinkClassApi: EdLinkClassApi,
    private val classroomService: ClassroomService,
    private val coTeacherService: CoTeacherService,
    private val studentProfileService: StudentProfileService
) : EdLinkRefreshTeacherClassesUseCase {

    override fun refresh(teacherProfile: TeacherProfile): Either<OperationError, Unit> = either {
        if (teacherProfile.edLinkIntegrationId == null || teacherProfile.edLinkPersonId == null)
            return TeacherNotLinkedToEdLinkError().left()

        val integration = edLinkIntegrationApi.getIntegration(teacherProfile.edLinkIntegrationId).bind()

        val edLinkClasses = edLinkClassApi.listClasses(integration.accessToken).bind()
            .filter {
                isEdLinkTeacherInEdLinkClass(integration.accessToken, teacherProfile.edLinkPersonId, it.id).bind()
            }

        edLinkClasses.forEach { edLinkClass ->
            val classroom: Classroom? = classroomService.getByEdLinkId(edLinkClass.id)
            if (classroom == null) {
                val edLinkStudents = edLinkClassApi.listStudents(integration.accessToken, edLinkClass.id).bind()
                // TODO: check for existing students, add to classes accordingly instead of creating new students
                val studentCreateRequests = edLinkStudents
                    .map { person -> StudentCreateRequest(person.displayName, integration.id, person.id) }
                val classroomCreateRequest = ClassroomCreateRequest(edLinkClass.name, "", edLinkClass.id)
                val newClassroom = classroomService.create(teacherProfile.id, classroomCreateRequest)
                studentProfileService.createStudents(newClassroom.id, studentCreateRequests)
            } else {
                coTeacherService.addCoTeacherToClassroom(classroom.id, teacherProfile)
            }
        }
    }

    private fun isEdLinkTeacherInEdLinkClass(
        accessToken: String,
        teacherId: UUID,
        classId: UUID
    ): Either<OperationError, Boolean> = either {
        return@either edLinkClassApi.listTeachers(accessToken, classId).bind()
            .any { teacher -> teacher.id == teacherId }
    }
}

sealed class EdLinkRefreshTeacherClassesError(val message: String) : OperationError
class TeacherNotLinkedToEdLinkError : EdLinkRefreshTeacherClassesError("TEACHER_NOT_LINKED_TO_EDLINK")
