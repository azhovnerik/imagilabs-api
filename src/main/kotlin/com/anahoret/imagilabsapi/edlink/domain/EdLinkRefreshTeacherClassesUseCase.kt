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
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacher
import com.anahoret.imagilabsapi.coteachers.domain.CoTeacherService
import com.anahoret.imagilabsapi.edlink.api.EdLinkClassApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkEnrollmentApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkIntegrationApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkSchoolApi
import com.anahoret.imagilabsapi.edlink.api.model.EdLinkClass
import com.anahoret.imagilabsapi.edlink.api.model.Enrollment
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
    private val edLinkEnrollmentApi: EdLinkEnrollmentApi,
    private val logger: Logger
) : EdLinkRefreshTeacherClassesUseCase {

    override fun refresh(teacherProfile: TeacherProfile): Either<OperationError, Unit> = either {
        if (teacherProfile.edLinkIntegrationId == null || teacherProfile.edLinkPersonId == null)
            return TeacherNotLinkedToEdLinkError().left()

        val integration = edLinkIntegrationApi.getIntegration(teacherProfile.edLinkIntegrationId).bind()
        val integrationEdLinkClasses = edLinkClassApi.listClasses(integration.accessToken).bind()
        val enrollmentClassIds =
            edLinkEnrollmentApi.listTeacherEnrollments(integration.accessToken, teacherProfile.edLinkPersonId).bind()
                .map(Enrollment::classId)
                .toSet()
        val teacherEdLinkClasses = integrationEdLinkClasses
            .filter { it.id in enrollmentClassIds }

        updateClasses(teacherEdLinkClasses, integration, teacherProfile)
        softDeleteDeletedEdLinkClasses(integrationEdLinkClasses, integration)
    }

    private fun updateClasses(
        edLinkClasses: List<EdLinkClass>,
        integration: Integration,
        teacherProfile: TeacherProfile
    ) {
        removeFromOwnedClassrooms(edLinkClasses, integration, teacherProfile)
        removeFromCoTeacherClasses(edLinkClasses, integration, teacherProfile)
        addToClasses(edLinkClasses, integration, teacherProfile)
    }

    private fun addToClasses(
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
                makeTeacherIfNeeded(classroom, teacherProfile)
                refreshStudents(classroom, integration)
            }
        }
    }

    private fun removeFromCoTeacherClasses(
        edLinkClasses: List<EdLinkClass>,
        integration: Integration,
        teacherProfile: TeacherProfile
    ) {
        val edLinkClassIds = edLinkClasses.map(EdLinkClass::id)
        coTeacherService.getClassroomIdListByTeacherId(teacherProfile.id)
            .let(classroomService::listByIds)
            .filter {
                it.isEdLinkConnected &&
                        it.edLinkIntegrationId == integration.id &&
                        it.edLinkClassId !in edLinkClassIds
            }.forEach { removedClassroom ->
                coTeacherService.deleteCoTeacher(removedClassroom.id, teacherProfile.id)
            }
    }

    private fun removeFromOwnedClassrooms(
        edLinkClasses: List<EdLinkClass>,
        integration: Integration,
        teacherProfile: TeacherProfile
    ) {
        val edLinkClassIds = edLinkClasses.map(EdLinkClass::id)
        classroomService.listByTeacher(teacherProfile.id)
            .filter {
                it.isEdLinkConnected &&
                        it.edLinkIntegrationId == integration.id &&
                        it.edLinkClassId !in edLinkClassIds
            }
            .forEach { removedClassroom ->
                val coTeachers = coTeacherService.getAllCoTeachersByClassroomId(removedClassroom.id)
                if (coTeachers.isEmpty()) {
                    classroomService.setTeacher(removedClassroom.id, null)
                } else {
                    val firstCoTeacher = coTeachers.minBy(CoTeacher::createdAt)
                    classroomService.setTeacher(removedClassroom.id, firstCoTeacher.id)
                    coTeacherService.deleteCoTeacher(removedClassroom.id, firstCoTeacher.id)
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

    private fun makeTeacherIfNeeded(
        classroom: Classroom,
        teacherProfile: TeacherProfile
    ) {
        if (classroom.teacherId == null) {
            classroomService.setTeacher(classroom.id, teacherProfile.id)
        } else if (
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
            val existingImagiStudentPersonIds = existingImagiStudents.map(StudentProfile::edLinkPersonId).toSet()
            val newEdLinkStudents = edLinkStudents.filterNot { it.id in existingImagiStudentPersonIds }
            val edLinkStudentIds = edLinkStudents.map(Person::id)
            val deletedStudents = existingImagiStudents.filterNot { it.edLinkPersonId in edLinkStudentIds }
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
            val existingImagiStudentPersonIds = existingImagiStudents.map(StudentProfile::edLinkPersonId).toSet()
            val newEdLinkStudents = edLinkStudents.filterNot { it.id in existingImagiStudentPersonIds }
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
}

sealed class EdLinkRefreshTeacherClassesError(val message: String) : OperationError
class TeacherNotLinkedToEdLinkError : EdLinkRefreshTeacherClassesError("TEACHER_NOT_LINKED_TO_EDLINK")
