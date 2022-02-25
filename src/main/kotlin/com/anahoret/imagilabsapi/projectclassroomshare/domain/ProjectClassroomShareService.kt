package com.anahoret.imagilabsapi.projectclassroomshare.domain

import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntity
import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntityRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

interface ProjectClassroomShareService {

    fun share(projectId: UUID, classroomId: UUID)
}

@Service
class ProjectClassroomShareServiceImpl(
    private val projectClassroomShareEntityRepository: ProjectClassroomShareEntityRepository
) : ProjectClassroomShareService {

    @Transactional
    override fun share(projectId: UUID, classroomId: UUID) {
        val projectSharedInClassroom =
            projectClassroomShareEntityRepository.existsByProjectIdAndClassroomId(projectId, classroomId)
        if (!projectSharedInClassroom) {
            projectClassroomShareEntityRepository.save(
                ProjectClassroomShareEntity(projectId, classroomId)
            )
        }
    }

}
