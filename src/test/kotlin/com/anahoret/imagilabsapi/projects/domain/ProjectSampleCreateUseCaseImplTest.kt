package com.anahoret.imagilabsapi.projects.domain

import arrow.core.right
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.projects.domain.usecases.ProjectSamplesCreateUseCaseImpl
import com.anahoret.imagilabsapi.pythoncompiler.domain.CodeRunUseCase
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeResponse
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Project sample create use case")
class ProjectSampleCreateUseCaseImplTest {

    private val projectService = mockk<ProjectService>()
    private val sampleProjectLoader = mockk<SampleProjectLoader>()
    private val codeRunUseCase = mockk<CodeRunUseCase>()
    private val objectMapper = mockk<ObjectMapper>()

    private val projectSamplesCreateUseCase = ProjectSamplesCreateUseCaseImpl(
        projectService, sampleProjectLoader, codeRunUseCase, objectMapper
    )

    @Test
    fun `should create sample project`() {
        val testTeacher = testTeacher()
        val projects = listOf(SampleProject(
            name = "Test sample project",
            sourceCode = "m[0][0] = on",
        ))
        val runCodeResponse = RunCodeResponse(null, emptyList())

        every { sampleProjectLoader.load() } returns projects
        every { codeRunUseCase.run(testTeacher, any()) } returns runCodeResponse.right()
        every { objectMapper.writeValueAsString(any()) } returns ""
        every { projectService.createSampleProject(testTeacher.id, any()) } returns mockk()

        projectSamplesCreateUseCase.create(testTeacher)

        verify { projectService.createSampleProject(testTeacher.id, any()) }
    }
}
