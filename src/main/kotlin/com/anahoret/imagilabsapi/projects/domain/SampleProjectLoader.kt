package com.anahoret.imagilabsapi.projects.domain

import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component
import org.springframework.util.ResourceUtils

interface SampleProjectLoader {

    fun load(): List<SampleProject>
}

@Component
class SampleProjectLoaderImpl : SampleProjectLoader {

    companion object {

        private const val DIRECTORY_PATH = "${ResourceUtils.CLASSPATH_URL_PREFIX}samplesprojects/*"
    }

    override fun load(): List<SampleProject> {
        return PathMatchingResourcePatternResolver(this.javaClass.classLoader)
            .getResources(DIRECTORY_PATH)
            .filter { it.exists() && it.isFile }
            .map { SampleProject(it.filename!!, it.getContentAsString(Charsets.UTF_8)) }
            .sortedByDescending(SampleProject::name)
    }
}
