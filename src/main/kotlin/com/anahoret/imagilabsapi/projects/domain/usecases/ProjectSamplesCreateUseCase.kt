package com.anahoret.imagilabsapi.projects.domain.usecases

import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.projects.domain.SampleProjectLoader
import org.springframework.stereotype.Service
import java.util.*

interface ProjectSamplesCreateUseCase {

    fun create(teacherId: UUID)
}

@Service
class ProjectSamplesCreateUseCaseImpl(
    private val projectService: ProjectService,
    private val sampleProjectLoader: SampleProjectLoader
) : ProjectSamplesCreateUseCase {

    override fun create(teacherId: UUID) {
        val projects = sampleProjectLoader.load()
        projectService.createSampleProject(teacherId, projects)
    }
}
