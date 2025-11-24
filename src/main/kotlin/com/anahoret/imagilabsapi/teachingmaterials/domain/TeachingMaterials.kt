package com.anahoret.imagilabsapi.teachingmaterials.domain

class TeachingMaterials(
    val teachingSlides: List<TeachingMaterial>,
    val worksheets: List<TeachingMaterial>,
) {
    companion object {
        fun empty() = TeachingMaterials(emptyList(), emptyList())
    }
}
