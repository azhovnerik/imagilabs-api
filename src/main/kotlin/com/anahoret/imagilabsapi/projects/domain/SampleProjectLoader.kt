package com.anahoret.imagilabsapi.projects.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.util.ResourceUtils

interface SampleProjectLoader {

    fun load(): List<SampleProject>
}

@Component
class SampleProjectLoaderImpl(
    @Value("${ResourceUtils.CLASSPATH_URL_PREFIX}samplesprojects/*")
    private val resources: Array<Resource>
): SampleProjectLoader {

    override fun load(): List<SampleProject> {
        return resources
            .filter { it.exists() && it.isFile }
            .map { SampleProject(it.nameWithoutExtension(), it.getContentAsString(Charsets.UTF_8)) }
            .sortedByDescending(SampleProject::name)
    }

    private fun Resource.nameWithoutExtension() = this.filename!!.substringBeforeLast(".")
}
