package com.anahoret.imagilabsapi.teachingmaterials.domain

import com.anahoret.imagilabsapi.teachingmaterials.storage.*
import org.springframework.stereotype.Service
import java.util.*

interface TeacherBundleService {

    fun create(teacherId: UUID, bundleId: UUID)
    fun getBundlesByTeacherId(teacherId: UUID): List<LessonBundle>
    fun deleteAllByTeacherId(teacherId: UUID)
    fun getBundleLessonsByTeacherId(teacherId: UUID): List<BundleLesson>
    fun getByTeacherIdAndBundleId(teacherId: UUID, bundleId: UUID): UUID?
    fun delete(teacherBundleId: UUID)
    fun hasLinkedBundle(teacherId: UUID, bundleId: UUID): Boolean
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

    override fun getBundlesByTeacherId(teacherId: UUID): List<LessonBundle> {
        val lessonBundlesEntities = lessonBundleEntityRepository.findAllByTeacherId(teacherId)
        val lessonBundlesEntitiesIds = lessonBundlesEntities.map { it.id!! }
        val bundleLessons = bundleLessonEntityRepository.findAllByBundleLessonsIds(lessonBundlesEntitiesIds)

        return mapToLessonBundles(lessonBundlesEntities, bundleLessons)
    }

    override fun deleteAllByTeacherId(teacherId: UUID) {
        teacherBundleRepository.deleteAllByTeacherId(teacherId)
    }

    override fun getBundleLessonsByTeacherId(teacherId: UUID): List<BundleLesson> {
        return bundleLessonEntityRepository.findAllBundleLessonsByTeacherId(teacherId)
            .map { BundleLesson.fromEntity(it) }
    }

    override fun getByTeacherIdAndBundleId(teacherId: UUID, bundleId: UUID): UUID? {
        return teacherBundleRepository.findByTeacherIdAndBundleId(teacherId, bundleId)?.id
    }

    override fun delete(teacherBundleId: UUID) {
        return teacherBundleRepository.deleteById(teacherBundleId)
    }

    override fun hasLinkedBundle(teacherId: UUID, bundleId: UUID): Boolean {
        return teacherBundleRepository.existsByTeacherIdAndBundleId(teacherId, bundleId)
    }

    private fun mapToLessonBundles(
        lessonBundlesEntities: List<LessonBundleEntity>,
        bundleLessons: List<BundleLessonEntity>
    ): List<LessonBundle> {
        val bundleLessonsMap = bundleLessons
            .map { BundleLesson.fromEntity(it) }
            .groupBy { it.bundleId }

        return lessonBundlesEntities.map {
            LessonBundle.fromEntity(it, bundleLessonsMap[it.id]!!)
        }
    }
}
