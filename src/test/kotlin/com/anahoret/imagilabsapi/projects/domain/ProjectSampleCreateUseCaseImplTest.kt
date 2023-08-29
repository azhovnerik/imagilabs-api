package com.anahoret.imagilabsapi.projects.domain

import com.anahoret.imagilabsapi.projects.domain.usecases.ProjectSamplesCreateUseCaseImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("Project sample create use case")
class ProjectSampleCreateUseCaseImplTest {

    private val projectService = mockk<ProjectService>()
    private val sampleProjectLoader = mockk<SampleProjectLoader>()
    private val projectSamplesCreateUseCase = ProjectSamplesCreateUseCaseImpl(projectService, sampleProjectLoader)

    private val teacherId = UUID.randomUUID()

    @Test
    fun `should create sample project`() {
        val projects = mockk<List<SampleProject>>()

        every { sampleProjectLoader.load() } returns projects
        every { projectService.createSampleProject(teacherId, projects) } returns mockk()

        projectSamplesCreateUseCase.create(teacherId)

        verify { projectService.createSampleProject(teacherId, projects) }
    }
}
