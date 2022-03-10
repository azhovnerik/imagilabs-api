package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.TeachingMaterialEntityRepository
import org.springframework.stereotype.Service

interface TeachingMaterialService {

    fun listSortedByIndex(): TeachingMaterials
}

@Service
class TeachingMaterialServiceImpl(
    private val teachingMaterialEntityRepository: TeachingMaterialEntityRepository
) : TeachingMaterialService {

    override fun listSortedByIndex(): TeachingMaterials {
        return teachingMaterialEntityRepository.findAll()
            .let { TeachingMaterials.groupByCategory(it) }
    }

}
