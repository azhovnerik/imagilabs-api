package com.anahoret.imagilabsapi.projects.domain.usecases

import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.projects.domain.SampleProjectLoader
import com.anahoret.imagilabsapi.pythoncompiler.domain.CodeRunUseCase
import com.anahoret.imagilabsapi.pythoncompiler.domain.RunCodeRequest
import com.anahoret.imagilabsapi.teachers.domain.TeacherProfile
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

interface ProjectSamplesCreateUseCase {

    fun create(teacherProfile: TeacherProfile)
}

@Service
class ProjectSamplesCreateUseCaseImpl(
    private val projectService: ProjectService,
    private val sampleProjectLoader: SampleProjectLoader,
    private val codeRunUseCase: CodeRunUseCase,
    private val objectMapper: ObjectMapper
) : ProjectSamplesCreateUseCase {

    override fun create(teacherProfile: TeacherProfile) {
        val projects = sampleProjectLoader.load()
        projects.map { sample ->
            val runCodeResponse = codeRunUseCase.run(teacherProfile, RunCodeRequest(sample.sourceCode))
            runCodeResponse.onRight { sample.runResult = objectMapper.writeValueAsString(it) }
        }

        projectService.createSampleProject(teacherProfile.id, projects)
    }
}
