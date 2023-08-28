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
    private val projectSamplesCreateUseCase = ProjectSamplesCreateUseCaseImpl(projectService)

    private val teacherId = UUID.randomUUID()

    @Test
    fun `should create sample project`() {
        every { projectService.createSampleProject(teacherId, any()) } returns mockk()
        projectSamplesCreateUseCase.create(teacherId)
        verify { projectService.createSampleProject(teacherId, any()) }
    }
}
