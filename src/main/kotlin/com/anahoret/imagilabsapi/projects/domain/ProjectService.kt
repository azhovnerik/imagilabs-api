package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.projects.storage.ProjectEntity
import com.anahoret.imagilabsapi.projects.storage.ProjectEntityRepository
import com.anahoret.imagilabsapi.pythoncompiler.RunCodeResponse
import com.anahoret.imagilabsapi.users.UserType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface ProjectService {

    fun createProject(ownerId: UUID, ownerType: UserType): Project
    fun getProjectById(projectId: UUID): Project?
    fun updateProject(projectId: UUID, projectUpdateRequest: ProjectUpdateRequest): Project?
    fun updateProjectRunResult(projectId: UUID, runCodeResponse: RunCodeResponse)
    fun listByIds(projectIds: Collection<UUID>, pageable: Pageable): Page<Project>
    fun getProjectCounts(ownerIds: Iterable<UUID>): Map<UUID, Long>
    fun listByOwnerId(ownerId: UUID, pageable: Pageable): Page<Project>
    fun delete(projectId: UUID)
    fun deleteAllByOwner(ownerId: UUID)
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
        ).let { Project.fromEntity(it, ::parseToRunCodeResponse) }
    }

    override fun getProjectById(projectId: UUID): Project? {
        return projectEntityRepository.findByIdOrNull(projectId)
            ?.let { Project.fromEntity(it, ::parseToRunCodeResponse) }
    }

    override fun updateProject(projectId: UUID, projectUpdateRequest: ProjectUpdateRequest): Project? {
        return projectEntityRepository.findByIdOrNull(projectId)?.let {
            it.name = projectUpdateRequest.name
            it.sourceCode = projectUpdateRequest.sourceCode
            projectEntityRepository.save(it)
        }?.let { Project.fromEntity(it, ::parseToRunCodeResponse) }
    }

    override fun updateProjectRunResult(projectId: UUID, runCodeResponse: RunCodeResponse) {
        projectEntityRepository.findByIdOrNull(projectId)?.let {
            val runResult = objectMapper.writeValueAsString(runCodeResponse)
            it.runResult = runResult
            projectEntityRepository.save(it)
        }
    }

    override fun listByIds(projectIds: Collection<UUID>, pageable: Pageable): Page<Project> {
        return projectEntityRepository.findAllByIdIn(projectIds, pageable)
            .map { Project.fromEntity(it, ::parseToRunCodeResponse) }
    }

    override fun listByOwnerId(ownerId: UUID, pageable: Pageable): Page<Project> {
        return projectEntityRepository.findAllByOwnerId(ownerId, pageable)
            .map { Project.fromEntity(it, ::parseToRunCodeResponse) }
    }

    override fun getProjectCounts(ownerIds: Iterable<UUID>): Map<UUID, Long> {
        return projectEntityRepository.countByOwnerIds(ownerIds)
            .associate { it.ownerId to it.projectsCount }
    }

    override fun delete(projectId: UUID) {
        projectEntityRepository.deleteById(projectId)
    }

    override fun deleteAllByOwner(ownerId: UUID) {
        projectEntityRepository.deleteAllByOwnerId(ownerId)
    }

    private fun parseToRunCodeResponse(json: String): RunCodeResponse {
        return objectMapper.readValue(json)
    }

}
