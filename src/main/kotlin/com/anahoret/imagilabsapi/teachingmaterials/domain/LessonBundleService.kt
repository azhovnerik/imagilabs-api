package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.BundleLessonEntity
import com.anahoret.imagilabsapi.teachingmaterials.storage.BundleLessonEntityRepository
import com.anahoret.imagilabsapi.teachingmaterials.storage.LessonBundleEntity
import com.anahoret.imagilabsapi.teachingmaterials.storage.LessonBundleEntityRepository
import org.springframework.stereotype.Service

interface LessonBundleService {

    fun create(lessonBundleDataRequest: LessonBundleDataRequest): LessonBundle
}

@Service
class LessonBundleServiceImpl(
    private val lessonBundleEntityRepository: LessonBundleEntityRepository,
    private val bundleLessonEntityRepository: BundleLessonEntityRepository
) : LessonBundleService {

    override fun create(lessonBundleDataRequest: LessonBundleDataRequest): LessonBundle {
        val bundleEntity = lessonBundleEntityRepository.save(LessonBundleEntity(lessonBundleDataRequest.name))
        val lessons = lessonBundleDataRequest.lessons
            .mapIndexed { index, lesson ->
                BundleLessonEntity(
                    bundleEntity.id!!,
                    index,
                    lesson.locked,
                    lesson.name,
                    lesson.worksheetUri,
                    lesson.slidesUri
                )
            }.let(bundleLessonEntityRepository::saveAll)
        return LessonBundle.fromEntity(bundleEntity, lessons.map { BundleLesson.fromEntity(it) })
    }
}
