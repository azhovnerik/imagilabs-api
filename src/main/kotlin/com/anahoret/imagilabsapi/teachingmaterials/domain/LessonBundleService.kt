package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.BundleLessonEntity
import com.anahoret.imagilabsapi.teachingmaterials.storage.BundleLessonEntityRepository
import com.anahoret.imagilabsapi.teachingmaterials.storage.LessonBundleEntity
import com.anahoret.imagilabsapi.teachingmaterials.storage.LessonBundleEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

interface LessonBundleService {

    fun create(lessonBundleDataRequest: LessonBundleDataRequest): LessonBundle
    fun update(bundleId: UUID, lessonBundleDataRequest: LessonBundleDataRequest): LessonBundle?
    fun list(): List<LessonBundleBase>
}

@Service
class LessonBundleServiceImpl(
    private val lessonBundleEntityRepository: LessonBundleEntityRepository,
    private val bundleLessonEntityRepository: BundleLessonEntityRepository
) : LessonBundleService {

    override fun create(lessonBundleDataRequest: LessonBundleDataRequest): LessonBundle {
        val bundleEntity = lessonBundleEntityRepository.save(LessonBundleEntity(lessonBundleDataRequest.name))
        val lessons = addBundleLessons(bundleEntity.id!!, lessonBundleDataRequest.lessons)
        return LessonBundle.fromEntity(bundleEntity, lessons.map { BundleLesson.fromEntity(it) })
    }

    @Transactional
    override fun update(bundleId: UUID, lessonBundleDataRequest: LessonBundleDataRequest): LessonBundle? {
        return lessonBundleEntityRepository.findByIdOrNull(bundleId)
            ?.let { bundleEntity ->
                bundleEntity.name = lessonBundleDataRequest.name
                lessonBundleEntityRepository.save(bundleEntity)

                bundleLessonEntityRepository.deleteAllByBundleId(bundleId)
                val lessons = addBundleLessons(bundleEntity.id!!, lessonBundleDataRequest.lessons)

                LessonBundle.fromEntity(bundleEntity, lessons.map { BundleLesson.fromEntity(it) })
            }
    }

    override fun list(): List<LessonBundleBase> {
        return lessonBundleEntityRepository.findAllByOrderByLastModifiedAt()
            .map(LessonBundleBase.Companion::fromEntity)
    }

    private fun addBundleLessons(
        bundleId: UUID,
        lessons: List<LessonData>
    ): Iterable<BundleLessonEntity> {
        return lessons.mapIndexed { index, lesson ->
            BundleLessonEntity(bundleId, index, lesson.locked, lesson.name, lesson.worksheetUri, lesson.slidesUri)
        }.let(bundleLessonEntityRepository::saveAll)
    }

}
