package com.anahoret.imagilabsapi.projectclassroomshare.domain

import com.anahoret.imagilabsapi.classrooms.domain.ClassroomSearchProjectsRequest
import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntity
import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntityRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

interface ProjectClassroomShareService {

    fun shareToAll(projectId: UUID, classroomIds: Iterable<UUID>)
    fun getProjectCountsByClassrooms(classroomIds: Iterable<UUID>): Map<UUID, Long>
    fun getProjectCountsByOwners(ownerIds: Iterable<UUID>): Map<UUID, Long>
    fun getProjectCount(classroomId: UUID): Long
    fun getShares(projectId: UUID): List<ProjectClassroomShare>
    fun listByClassroom(classroomId: UUID, searchRequest: ClassroomSearchProjectsRequest): List<ProjectClassroomShare>
    fun listByOwnerId(ownerId: UUID): List<ProjectClassroomShare>
    fun unshareFromAll(projectId: UUID)
    fun unshareFromAll(projectId: UUID, classroomIds: List<UUID>)
    fun unshareFromAll(projectIds: Collection<UUID>)
    fun unshareAllFrom(classroomId: UUID)
    fun unshareProjectsFromClassroom(projectIds: List<UUID>, classroomId: UUID)
}

@Service
class ProjectClassroomShareServiceImpl(
    private val projectClassroomShareEntityRepository: ProjectClassroomShareEntityRepository
) : ProjectClassroomShareService {

    @Transactional(rollbackOn = [Throwable::class])
    override fun shareToAll(projectId: UUID, classroomIds: Iterable<UUID>) {
        val alreadySharedIn = projectClassroomShareEntityRepository.findAllByProjectId(projectId)
            .map { it.classroomId }
            .toSet()
        classroomIds
            .filter { it !in alreadySharedIn }
            .map { classroomId -> ProjectClassroomShareEntity(projectId, classroomId) }
            .let { projectClassroomShareEntityRepository.saveAll(it) }
    }

    @Transactional(rollbackOn = [Throwable::class])
    override fun unshareFromAll(projectId: UUID, classroomIds: List<UUID>) {
        if (classroomIds.isEmpty()) return
        projectClassroomShareEntityRepository.deleteAllByProjectIdAndClassroomIdIn(projectId, classroomIds)
    }

    @Transactional(rollbackOn = [Throwable::class])
    override fun unshareFromAll(projectId: UUID) {
        projectClassroomShareEntityRepository.deleteAllByProjectId(projectId)
    }

    override fun unshareFromAll(projectIds: Collection<UUID>) {
        if (projectIds.isEmpty()) return
        projectClassroomShareEntityRepository.deleteAllByProjectIdIn(projectIds)
    }

    override fun unshareAllFrom(classroomId: UUID) {
        projectClassroomShareEntityRepository.deleteAllByClassroomId(classroomId)
    }

    @Transactional
    override fun unshareProjectsFromClassroom(projectIds: List<UUID>, classroomId: UUID) {
        projectClassroomShareEntityRepository.deleteAllByClassroomIdAndProjectIdIn(classroomId, projectIds)
    }

    override fun getProjectCountsByClassrooms(classroomIds: Iterable<UUID>): Map<UUID, Long> {
        return projectClassroomShareEntityRepository.getProjectCountsByClassrooms(classroomIds)
            .associate { it.classroomId to it.projectsCount }
    }

    override fun getProjectCountsByOwners(ownerIds: Iterable<UUID>): Map<UUID, Long> {
        return projectClassroomShareEntityRepository.getProjectCountsByOwners(ownerIds)
            .associate { it.ownerId to it.projectsCount }
    }

    override fun getProjectCount(classroomId: UUID): Long {
        return projectClassroomShareEntityRepository.countByClassroomId(classroomId)
    }

    override fun getShares(projectId: UUID): List<ProjectClassroomShare> {
        return projectClassroomShareEntityRepository.findAllByProjectId(projectId)
            .map(ProjectClassroomShare.Companion::fromEntity)
    }

    override fun listByClassroom(
        classroomId: UUID,
        searchRequest: ClassroomSearchProjectsRequest
    ): List<ProjectClassroomShare> {
        return with(searchRequest) { projectClassroomShareEntityRepository.search(classroomId, ownerId, searchQuery) }
            .map(ProjectClassroomShare.Companion::fromEntity)
    }

    override fun listByOwnerId(ownerId: UUID): List<ProjectClassroomShare> {
        return projectClassroomShareEntityRepository.findAllByOwnerId(ownerId)
            .map(ProjectClassroomShare.Companion::fromEntity)
    }

}
