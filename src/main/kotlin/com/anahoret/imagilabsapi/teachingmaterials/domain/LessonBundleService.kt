package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionPlan
import com.anahoret.imagilabsapi.teachingmaterials.storage.BundleLessonEntity
import com.anahoret.imagilabsapi.teachingmaterials.storage.BundleLessonEntityRepository
import com.anahoret.imagilabsapi.teachingmaterials.storage.LessonBundleEntity
import com.anahoret.imagilabsapi.teachingmaterials.storage.LessonBundleEntityRepository
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface LessonBundleService {

    fun create(lessonBundleDataRequest: LessonBundleDataRequest): LessonBundle
    fun update(bundleId: UUID, lessonBundleDataRequest: LessonBundleDataRequest): LessonBundle?
    fun list(searchQuery: String?, sort: Sort): List<LessonBundleBase>
    fun delete(bundleId: UUID)
    fun get(bundleId: UUID): LessonBundle?
    fun getDefaultBundle(includePro: Boolean): LessonBundle?
    fun exists(bundleId: UUID): Boolean
}

@Service
class LessonBundleServiceImpl(
    private val lessonBundleEntityRepository: LessonBundleEntityRepository,
    private val bundleLessonEntityRepository: BundleLessonEntityRepository,
    private val entityManager: EntityManager
) : LessonBundleService {

    override fun create(lessonBundleDataRequest: LessonBundleDataRequest): LessonBundle {
        if (lessonBundleDataRequest.defaultBundle) unsetDefaultBundle()
        val bundleEntity = lessonBundleEntityRepository.save(
            LessonBundleEntity(
                lessonBundleDataRequest.name,
                lessonBundleDataRequest.defaultBundle
            )
        )
        val lessons = addBundleLessons(bundleEntity.id!!, lessonBundleDataRequest.lessons)
        return LessonBundle.fromEntity(bundleEntity, lessons.map { BundleLesson.fromEntity(it) })
    }

    @Transactional
    override fun update(bundleId: UUID, lessonBundleDataRequest: LessonBundleDataRequest): LessonBundle? {
        return lessonBundleEntityRepository.findByIdOrNull(bundleId)
            ?.let { bundleEntity ->
                if (lessonBundleDataRequest.defaultBundle) unsetDefaultBundle()

                bundleEntity.name = lessonBundleDataRequest.name
                bundleEntity.defaultBundle = lessonBundleDataRequest.defaultBundle
                lessonBundleEntityRepository.save(bundleEntity)

                bundleLessonEntityRepository.deleteAllByBundleId(bundleId)
                /* Flush is needed because by default Hibernate runs inserts before deletes.
                    This causes unique constraint exception on (bundleId, lessonIndex) unique key.
                    So we delete old lessons, then flush and then insert new lessons.
                 */
                entityManager.flush()
                val lessons = addBundleLessons(bundleEntity.id!!, lessonBundleDataRequest.lessons)

                LessonBundle.fromEntity(bundleEntity, lessons.map { BundleLesson.fromEntity(it) })
            }
    }

    override fun list(searchQuery: String?, sort: Sort): List<LessonBundleBase> {
        return (
            searchQuery?.let { lessonBundleEntityRepository.findAllByNameContainingIgnoreCase(searchQuery, sort) }
                ?: lessonBundleEntityRepository.findAll(sort)
            ).map(LessonBundleBase.Companion::fromEntity)
    }

    @Transactional
    override fun delete(bundleId: UUID) {
        bundleLessonEntityRepository.deleteAllByBundleId(bundleId)
        lessonBundleEntityRepository.deleteById(bundleId)
    }

    override fun get(bundleId: UUID): LessonBundle? {
        return lessonBundleEntityRepository.findByIdOrNull(bundleId)?.let { bundleEntity ->
            val lessons = bundleLessonEntityRepository.findAllByBundleIdOrderByIndex(bundleId)
                .map { BundleLesson.fromEntity(it) }
            LessonBundle.fromEntity(bundleEntity, lessons)
        }
    }

    override fun getDefaultBundle(includePro: Boolean): LessonBundle? {
        return lessonBundleEntityRepository.findByDefaultBundleTrue()?.let { bundleEntity ->
            val lessons = bundleLessonEntityRepository.findBundlesByIncludedProOrderedByIndex(bundleEntity.id!!, includePro)
                .map { BundleLesson.fromEntity(it) }
            LessonBundle.fromEntity(bundleEntity, lessons)
        }
    }

    override fun exists(bundleId: UUID): Boolean {
        return lessonBundleEntityRepository.existsById(bundleId)
    }

    private fun addBundleLessons(
        bundleId: UUID,
        lessons: List<LessonData>
    ): Iterable<BundleLessonEntity> {
        return lessons.mapIndexed { index, lesson ->
            BundleLessonEntity(bundleId, index, lesson.proLesson, lesson.name, lesson.worksheetUri, lesson.slidesUri)
        }.let(bundleLessonEntityRepository::saveAll)
    }

    private fun unsetDefaultBundle() {
        lessonBundleEntityRepository.findByDefaultBundleTrue()?.let { defaultBundleEntity ->
            defaultBundleEntity.defaultBundle = false
            lessonBundleEntityRepository.save(defaultBundleEntity)
        }
    }

}
