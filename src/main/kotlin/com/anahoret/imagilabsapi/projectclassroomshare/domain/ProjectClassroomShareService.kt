package com.anahoret.imagilabsapi.projectclassroomshare.domain

import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntity
import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntityRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

interface ProjectClassroomShareService {

    fun shareToAll(projectId: UUID, classroomIds: Iterable<UUID>)
    fun getProjectCountsByClassrooms(classroomIds: Iterable<UUID>): Map<UUID, Long>
    fun getProjectCountsByOwners(ownerIds: Iterable<UUID>): Map<UUID, Long>
    fun getProjectCount(classroomId: UUID): Long
    fun getShares(projectId: UUID): List<ProjectClassroomShare>
    fun listByClassroom(classroomId: UUID): List<ProjectClassroomShare>
    fun listByOwnerId(ownerId: UUID): List<ProjectClassroomShare>
    fun unshareFromAll(projectId: UUID, classroomIds: List<UUID>)
}

@Service
class ProjectClassroomShareServiceImpl(
    private val projectClassroomShareEntityRepository: ProjectClassroomShareEntityRepository
) : ProjectClassroomShareService {

    @Transactional
    override fun shareToAll(projectId: UUID, classroomIds: Iterable<UUID>) {
        val alreadySharedIn = projectClassroomShareEntityRepository.findAllByProjectId(projectId)
            .map { it.classroomId }
            .toSet()
        classroomIds
            .filter { it !in alreadySharedIn }
            .map { classroomId -> ProjectClassroomShareEntity(projectId, classroomId) }
            .let { projectClassroomShareEntityRepository.saveAll(it) }
    }

    @Transactional
    override fun unshareFromAll(projectId: UUID, classroomIds: List<UUID>) {
        if (classroomIds.isEmpty()) return
        projectClassroomShareEntityRepository.deleteAllByProjectIdAndClassroomIdIn(projectId, classroomIds)
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

    override fun listByClassroom(classroomId: UUID): List<ProjectClassroomShare> {
        return projectClassroomShareEntityRepository.findAllByClassroomId(classroomId)
            .map(ProjectClassroomShare.Companion::fromEntity)
    }

    override fun listByOwnerId(ownerId: UUID): List<ProjectClassroomShare> {
        return projectClassroomShareEntityRepository.findAllByOwnerId(ownerId)
            .map(ProjectClassroomShare.Companion::fromEntity)
    }

}
