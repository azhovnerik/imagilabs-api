package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.projects.storage.ProjectEntity
import com.anahoret.imagilabsapi.projects.storage.ProjectEntityRepository
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface ProjectService {

    fun createProject(ownerId: UUID, ownerType: UserType): Project
    fun getProjectById(projectId: UUID): Project?
    fun updateProject(projectId: UUID, projectUpdateRequest: ProjectUpdateRequest): Project?
}

@Service
class ProjectServiceImpl(
    private val projectEntityRepository: ProjectEntityRepository
) : ProjectService {

    override fun createProject(ownerId: UUID, ownerType: UserType): Project {
        return projectEntityRepository.save(
            ProjectEntity(
                name = "",
                ownerId,
                ownerType,
                sourceCode = ""
            )
        ).let(Project.Companion::fromEntity)
    }

    override fun getProjectById(projectId: UUID): Project? {
        return projectEntityRepository.findByIdOrNull(projectId)
            ?.let(Project.Companion::fromEntity)
    }

    override fun updateProject(projectId: UUID, projectUpdateRequest: ProjectUpdateRequest): Project? {
        return projectEntityRepository.findByIdOrNull(projectId)?.let {
            it.name = projectUpdateRequest.name
            it.sourceCode = projectUpdateRequest.sourceCode
            projectEntityRepository.save(it)
        }?.let(Project.Companion::fromEntity)
    }

}
