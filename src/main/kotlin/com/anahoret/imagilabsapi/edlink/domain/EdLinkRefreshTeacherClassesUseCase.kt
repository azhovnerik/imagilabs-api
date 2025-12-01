package com.anahoret.imagilabsapi.edlink.domain

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomCreateRequest
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import com.anahoret.imagilabsapi.classrooms.domain.ClassroomUpdateRequest
import com.anahoret.imagilabsapi.common.domain.error.NotFoundError
import com.anahoret.imagilabsapi.common.domain.error.OperationError
import com.anahoret.imagilabsapi.common.domain.profiles.SystemProfile
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.edlink.api.EdLinkClassApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkIntegrationApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkSchoolApi
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkClass
import com.anahoret.imagilabsapi.edlink.api.model.Integration
import com.anahoret.imagilabsapi.edlink.api.model.Person
import com.anahoret.imagilabsapi.students.domain.StudentCreateRequest
import com.anahoret.imagilabsapi.students.domain.StudentDeleteUseCase
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.slf4j.Logger
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
    private val studentProfileService: StudentProfileService,
    private val studentClassroomLinkService: StudentClassroomLinkService,
    private val studentDeleteUseCase: StudentDeleteUseCase,
    private val edLinkSchoolApi: EdLinkSchoolApi,
    private val logger: Logger
) : EdLinkRefreshTeacherClassesUseCase {

    override fun refresh(teacherProfile: TeacherProfile): Either<OperationError, Unit> = either {
        if (teacherProfile.edLinkIntegrationId == null || teacherProfile.edLinkPersonId == null)
            return TeacherNotLinkedToEdLinkError().left()

        val integration = edLinkIntegrationApi.getIntegration(teacherProfile.edLinkIntegrationId).bind()
        val edLinkClasses = edLinkClassApi.listClasses(integration.accessToken).bind()
            .filter {
                isEdLinkTeacherInEdLinkClass(integration.accessToken, teacherProfile.edLinkPersonId, it.id).bind()
            }

        updateClasses(edLinkClasses, integration, teacherProfile)
        softDeleteDeletedEdLinkClasses(edLinkClasses, integration)
    }

    private fun updateClasses(
        edLinkClasses: List<EdLinkClass>,
        integration: Integration,
        teacherProfile: TeacherProfile
    ) {
        edLinkClasses.forEach { edLinkClass ->
            val classroom = classroomService.getByEdLinkId(integration.id, edLinkClass.id)
            if (classroom == null) {
                importClassroom(integration, edLinkClass, teacherProfile)
            } else if (!classroom.deleted) {
                refreshClassInfo(edLinkClass, classroom, integration)
                makeCoTeacherIfNeeded(classroom, teacherProfile)
                refreshStudents(classroom, integration)
            }
        }
    }

    private fun refreshClassInfo(
        edLinkClass: EdLinkClass,
        classroom: Classroom,
        integration: Integration
    ): Either<OperationError, Classroom> = either {
        val school = edLinkSchoolApi.getSchool(integration.accessToken, edLinkClass.schoolId).bind()
        classroomService.update(
            classroom.id,
            ClassroomUpdateRequest(
                name = edLinkClass.name,
                schoolName = school.name,
                studentNames = ""
            )
        ) ?: NotFoundError("CLASSROOM_NOT_FOUND").left().bind()
    }


    private fun softDeleteDeletedEdLinkClasses(
        edLinkClasses: List<EdLinkClass>,
        integration: Integration
    ) {
        val edLinkClassesIds = edLinkClasses.map(EdLinkClass::id).toSet()
        classroomService.listByEdLinkIntegration(integration.id)
            .filter { it.edLinkClassId !in edLinkClassesIds }
            .forEach { classroomService.softDelete(it.id) }
    }

    private fun makeCoTeacherIfNeeded(
        classroom: Classroom,
        teacherProfile: TeacherProfile
    ) {
        if (
            classroom.teacherId != teacherProfile.id &&
            !coTeacherService.isLinkedToClassroom(classroom.id, teacherProfile.id)
        ) {
            coTeacherService.addCoTeacherToClassroom(classroom.id, teacherProfile)
        }
    }

    private fun refreshStudents(
        classroom: Classroom,
        integration: Integration
    ): Either<OperationError, Unit> {
        val edLinkClassId = classroom.edLinkClassId ?: return NotFoundError("CLASSROOM_NOT_FOUND").left()
        return either {
            val edLinkStudents = edLinkClassApi.listStudents(integration.accessToken, edLinkClassId).bind()
            val existingImagiStudents =
                studentProfileService.listByEdLinkIds(integration.id, edLinkStudents.map(Person::id))
            val existingImagiStudentIds = existingImagiStudents.map(StudentProfile::id).toSet()
            val newEdLinkStudents = edLinkStudents.filterNot { it.id in existingImagiStudentIds }
            val edLinkStudentIds = edLinkStudents.map(Person::id)
            val deletedStudents = existingImagiStudents.filterNot { it.id in edLinkStudentIds }
            deletedStudents.forEach { studentDeleteUseCase.delete(SystemProfile, it.id).bind() }
            val studentCreateRequests = newEdLinkStudents
                .map { person -> StudentCreateRequest(person.displayName, integration.id, person.id) }
            val newImagiStudents = studentProfileService.createStudents(classroom.id, studentCreateRequests)
            (newImagiStudents + existingImagiStudents).map(StudentProfile::id)
                .let { studentIds ->
                    studentClassroomLinkService.addStudentsToClassroom(studentIds, classroom.id)
                }
        }.onLeft { logger.error("Failed to refresh students for classroom ${classroom.id}, $it") }
    }

    private fun importClassroom(
        integration: Integration,
        edLinkClass: EdLinkClass,
        teacherProfile: TeacherProfile
    ): Either<OperationError, Unit> {
        return either {
            val edLinkStudents = edLinkClassApi.listStudents(integration.accessToken, edLinkClass.id).bind()
            val existingImagiStudents =
                studentProfileService.listByEdLinkIds(integration.id, edLinkStudents.map(Person::id))
            val existingImagiStudentIds = existingImagiStudents.map(StudentProfile::id).toSet()
            val newEdLinkStudents = edLinkStudents.filterNot { it.id in existingImagiStudentIds }
            val studentCreateRequests = newEdLinkStudents
                .map { person -> StudentCreateRequest(person.displayName, integration.id, person.id) }
            val school = edLinkSchoolApi.getSchool(integration.accessToken, edLinkClass.schoolId).bind()
            val classroomCreateRequest = ClassroomCreateRequest(
                name = edLinkClass.name,
                studentNames = "",
                edLinkIntegrationId = integration.id,
                edLinkClassId = edLinkClass.id,
                schoolName = school.name
            )
            val newClassroom = classroomService.create(teacherProfile.id, classroomCreateRequest)
            val newStudents = studentProfileService.createStudents(newClassroom.id, studentCreateRequests)
            (existingImagiStudents + newStudents)
                .map(StudentProfile::id)
                .let { studentIds ->
                    studentClassroomLinkService.addStudentsToClassroom(studentIds, newClassroom.id)
                }
        }.onLeft { logger.error("Failed to import classroom ${edLinkClass.id}, $it") }
    }

    private fun isEdLinkTeacherInEdLinkClass(
        accessToken: String,
        teacherId: UUID,
        classId: UUID
    ): Either<OperationError, Boolean> = either {
        edLinkClassApi.listTeachers(accessToken, classId).bind()
            .any { teacher -> teacher.id == teacherId }
    }
}

sealed class EdLinkRefreshTeacherClassesError(val message: String) : OperationError
class TeacherNotLinkedToEdLinkError : EdLinkRefreshTeacherClassesError("TEACHER_NOT_LINKED_TO_EDLINK")
