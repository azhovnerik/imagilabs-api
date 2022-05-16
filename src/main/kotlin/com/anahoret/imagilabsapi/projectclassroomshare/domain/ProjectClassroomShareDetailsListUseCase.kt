package com.anahoret.imagilabsapi.projectclassroomshare.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomService
import org.springframework.stereotype.Service
import java.util.*

interface ProjectClassroomShareDetailsListUseCase {

    fun getShareDetails(projectId: UUID): List<ProjectClassroomShareDetails>
}

@Service
class ProjectClassroomShareDetailsListUseCaseImpl(
    private val projectClassroomShareService: ProjectClassroomShareService,
    private val classroomService: ClassroomService
) : ProjectClassroomShareDetailsListUseCase {

    override fun getShareDetails(projectId: UUID): List<ProjectClassroomShareDetails> {
        return projectClassroomShareService.getShares(projectId)
            .map { it.classroomId }
            .let(classroomService::listByIds)
            .map(ProjectClassroomShareDetails.Companion::fromClassroom)
    }
}
