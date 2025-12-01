package com.anahoret.imagilabsapi.projects.domain.usecases

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.projects.domain.Project
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfileService
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service

interface ProjectOwnerGetUseCase {

    fun get(project: Project): UserProfile?
}

@Service
class ProjectOwnerGetUseCaseImpl(
    private val teacherProfileService: TeacherProfileService,
    private val studentProfileService: StudentProfileService
) : ProjectOwnerGetUseCase {

    override fun get(project: Project): UserProfile? {
        return when (project.ownerUserType) {
            UserType.TEACHER -> teacherProfileService.getTeacherById(project.ownerId)
            UserType.STUDENT -> studentProfileService.getStudentById(project.ownerId)
            else -> null // Other user types cannot have projects at the moment
        }
    }
}
