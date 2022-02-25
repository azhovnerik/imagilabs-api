package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.projects.storage.ProjectEntity
import com.anahoret.imagilabsapi.projects.storage.ProjectEntityRepository
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.stereotype.Service
import java.util.*

interface ProjectService {

    fun createProject(ownerId: UUID, ownerType: UserType): Project
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

}
