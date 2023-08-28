package com.anahoret.imagilabsapi.projects.domain.usecases

import com.anahoret.imagilabsapi.projects.domain.ProjectService
import com.anahoret.imagilabsapi.projects.domain.SampleProject
import com.anahoret.imagilabsapi.utils.FileUtils
import org.springframework.stereotype.Service
import java.util.*

interface ProjectSamplesCreateUseCase {
    fun create(teacherId: UUID)
}

@Service
class ProjectSamplesCreateUseCaseImpl(
    private val projectService: ProjectService
) : ProjectSamplesCreateUseCase {

    companion object {
        const val DIRECTORY_PATH = "src/main/resources/samplesprojects"
    }

    override fun create(teacherId: UUID) {
        val requests = createSampleProjectRequests()
        projectService.createSampleProject(teacherId, requests)
    }

    private fun createSampleProjectRequests(): List<SampleProject> {
        return FileUtils.readDirectoryFiles(DIRECTORY_PATH)
            .map { SampleProject(it.fileName, it.text) }
    }

}
