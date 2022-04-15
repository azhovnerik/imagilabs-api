package com.anahoret.imagilabsapi.applicationproperties.domain

import com.anahoret.imagilabsapi.applicationproperties.storage.ApplicationPropertiesRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

interface ApplicationPropertiesService {

    fun getProperty(key: String): String
    fun getPropertyOrNull(key: String): String?
}

@Service
class ApplicationPropertiesServiceImpl(
    private val applicationPropertiesRepository: ApplicationPropertiesRepository
) : ApplicationPropertiesService {

    override fun getProperty(key: String): String {
        return applicationPropertiesRepository.findById(key).get().value
    }

    override fun getPropertyOrNull(key: String): String? {
        return applicationPropertiesRepository.findByIdOrNull(key)?.value
    }

}
