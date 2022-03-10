package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.TeachingMaterialEntity

class TeachingMaterials(
    val teachingSlides: List<TeachingMaterial>,
    val worksheets: List<TeachingMaterial>,
) {

    companion object {

        fun groupByCategory(teachingMaterialEntities: Iterable<TeachingMaterialEntity>): TeachingMaterials {
            val grouped = teachingMaterialEntities
                .map(TeachingMaterial.Companion::fromEntity)
                .groupBy { it.category }
            val teachingSlides = grouped[TeachingMaterialCategory.TEACHING_SLIDES]?.sortedBy { it.index }.orEmpty()
            val worksheets = grouped[TeachingMaterialCategory.WORKSHEETS]?.sortedBy { it.index }.orEmpty()
            return TeachingMaterials(
                teachingSlides = teachingSlides,
                worksheets = worksheets
            )
        }
    }
}
