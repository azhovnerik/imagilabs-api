package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.*
import org.springframework.stereotype.Service
import java.util.*

interface TeacherBundleService {

    fun create(teacherId: UUID, bundleId: UUID)
    fun getBundlesByTeacherId(teacherId: UUID, includePro: Boolean): List<LessonBundle>
    fun deleteAllByTeacherId(teacherId: UUID)
    fun getBundleLessonsByTeacherId(teacherId: UUID, includePro: Boolean): List<BundleLesson>
    fun exists(teacherBundleId: UUID): Boolean
    fun delete(teacherBundleId: UUID)
}

@Service
class TeacherBundleServiceImpl(
    private val teacherBundleRepository: TeacherBundleRepository,
    private val lessonBundleEntityRepository: LessonBundleEntityRepository,
    private val bundleLessonEntityRepository: BundleLessonEntityRepository,
) : TeacherBundleService {

    override fun create(teacherId: UUID, bundleId: UUID) {
        teacherBundleRepository.save(
            TeacherBundleEntity(teacherId, bundleId)
        )
    }

    override fun getBundlesByTeacherId(teacherId: UUID, includePro: Boolean): List<LessonBundle> {
        val lessonBundlesEntities = lessonBundleEntityRepository.findAllByTeacherId(teacherId)
        val lessonBundlesEntitiesIds = lessonBundlesEntities.map { it.id!! }
        val bundleLessonsMap = bundleLessonEntityRepository.findAllByBundleIds(lessonBundlesEntitiesIds, includePro)
            .map { BundleLesson.fromEntity(it) }
            .groupBy { it.bundleId }

        return mapToLessonBundles(lessonBundlesEntities, bundleLessonsMap)
    }

    override fun deleteAllByTeacherId(teacherId: UUID) {
        teacherBundleRepository.deleteAllByTeacherId(teacherId)
    }

    override fun getBundleLessonsByTeacherId(teacherId: UUID, includePro: Boolean): List<BundleLesson> {
        return bundleLessonEntityRepository.findAllBundleLessonsByTeacherId(teacherId, includePro)
            .map { BundleLesson.fromEntity(it) }
    }

    override fun exists(teacherBundleId: UUID): Boolean {
        return teacherBundleRepository.existsById(teacherBundleId)
    }

    override fun delete(teacherBundleId: UUID) {
        return teacherBundleRepository.deleteById(teacherBundleId)
    }

    private fun mapToLessonBundles(
        lessonBundlesEntities: List<LessonBundleEntity>,
        bundleLessonsMap: Map<UUID, List<BundleLesson>>
    ): List<LessonBundle> {
        return lessonBundlesEntities.map {
            LessonBundle.fromEntity(it, bundleLessonsMap[it.id]!!)
        }
    }
}
