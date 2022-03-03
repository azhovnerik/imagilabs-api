package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.projects.storage.ProjectEntity
import com.anahoret.imagilabsapi.projects.storage.ProjectEntityRepository
import com.anahoret.imagilabsapi.pythoncompiler.RunCodeResponse
import com.anahoret.imagilabsapi.users.UserType
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface ProjectService {

    fun createProject(ownerId: UUID, ownerType: UserType): Project
    fun getProjectById(projectId: UUID): Project?
    fun updateProject(projectId: UUID, projectUpdateRequest: ProjectUpdateRequest): Project?
    fun updateProjectRunResult(projectId: UUID, runCodeResponse: RunCodeResponse)
    fun listByIds(projectIds: Iterable<UUID>): List<Project>
    fun getProjectCounts(ownerIds: Iterable<UUID>): Map<UUID, Long>
}

@Service
class ProjectServiceImpl(
    private val projectEntityRepository: ProjectEntityRepository,
    private val objectMapper: ObjectMapper
) : ProjectService {

    override fun createProject(ownerId: UUID, ownerType: UserType): Project {
        return projectEntityRepository.save(
            ProjectEntity(
                name = "",
                ownerId,
                ownerType,
                sourceCode = ""
            )
        ).let { Project.fromEntity(it, objectMapper) }
    }

    override fun getProjectById(projectId: UUID): Project? {
        return projectEntityRepository.findByIdOrNull(projectId)
            ?.let { Project.fromEntity(it, objectMapper) }
    }

    override fun updateProject(projectId: UUID, projectUpdateRequest: ProjectUpdateRequest): Project? {
        return projectEntityRepository.findByIdOrNull(projectId)?.let {
            it.name = projectUpdateRequest.name
            it.sourceCode = projectUpdateRequest.sourceCode
            projectEntityRepository.save(it)
        }?.let { Project.fromEntity(it, objectMapper) }
    }

    override fun updateProjectRunResult(projectId: UUID, runCodeResponse: RunCodeResponse) {
        projectEntityRepository.findByIdOrNull(projectId)?.let {
            val runResult = objectMapper.writeValueAsString(runCodeResponse)
            it.runResult = runResult
            projectEntityRepository.save(it)
        }
    }

    override fun listByIds(projectIds: Iterable<UUID>): List<Project> {
        return projectEntityRepository.findAllById(projectIds)
            .map { Project.fromEntity(it, objectMapper) }
    }

    override fun getProjectCounts(ownerIds: Iterable<UUID>): Map<UUID, Long> {
        return projectEntityRepository.countByOwnerIds(ownerIds)
            .associate { it.ownerId to it.projectsCount }
    }

}
