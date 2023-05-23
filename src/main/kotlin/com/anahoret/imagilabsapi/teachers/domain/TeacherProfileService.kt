package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

interface TeacherProfileService {

    fun createTeacher(request: TeacherSignupRequest): TeacherProfile
    fun getTeacherById(id: UUID): TeacherProfile?
    fun getTeacherIdByEmail(email: String): UUID?
    fun getTeacherByIdForAdmin(id: UUID): TeacherProfileAdminView?
    fun getTeacherCredentialsByEmail(email: String): TeacherCredentials?
    fun getTeacherCredentialsById(teacherId: UUID): TeacherCredentials?
    fun exists(email: String): Boolean
    fun exists(teacherId: UUID): Boolean
    fun listByIds(ids: Iterable<UUID>): List<TeacherProfile>
    fun listAllForAdmin(searchQuery: String?, sort: Sort): List<TeacherProfileAdminView>
    fun listForAdmin(excludeIds: List<UUID>, sort: Sort): List<TeacherProfileAdminView>
    fun setPassword(email: String, newPassword: String)
    fun delete(teacherId: UUID)
}

@Service
class TeacherProfileServiceImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    private val passwordEncoder: PasswordEncoder,
    private val teacherSubscriptionService: TeacherSubscriptionService
) : TeacherProfileService {

    override fun createTeacher(request: TeacherSignupRequest): TeacherProfile {
        return with(request) {
            teacherProfileEntityRepository.save(
                TeacherProfileEntity(
                    email,
                    passwordEncoder.encode(password),
                    firstName,
                    lastName,
                    country,
                    organization,
                    howDidYouHearAboutUs,
                    howDidYouHearAboutUsOther,
                    marketingEmailSubscribed,
                )
            ).let {
                val subscription =
                    teacherSubscriptionService.buildSubscriptionDto(it)
                TeacherProfile.fromEntity(it, subscription)
            }
        }
    }

    override fun getTeacherById(id: UUID): TeacherProfile? {
        return teacherProfileEntityRepository.findByIdOrNull(id)
            ?.let {
                val subscription =
                    teacherSubscriptionService.buildSubscriptionDto(it)
                TeacherProfile.fromEntity(it, subscription)
            }
    }

    override fun getTeacherIdByEmail(email: String): UUID? {
        return teacherProfileEntityRepository.findByEmail(email)?.id
    }

    override fun getTeacherByIdForAdmin(id: UUID): TeacherProfileAdminView? {
        return teacherProfileEntityRepository.findByIdOrNull(id)
            ?.let {
                val subscription =
                    teacherSubscriptionService.buildSubscriptionDto(it)
                TeacherProfileAdminView.fromEntity(it, subscription)
            }
    }

    override fun listByIds(ids: Iterable<UUID>): List<TeacherProfile> {
        return teacherProfileEntityRepository.findAllById(ids)
            .map {
                val subscription =
                    teacherSubscriptionService.buildSubscriptionDto(it)
                TeacherProfile.fromEntity(it, subscription)
            }
    }

    override fun listAllForAdmin(searchQuery: String?, sort: Sort): List<TeacherProfileAdminView> {
        return (
            searchQuery?.let { teacherProfileEntityRepository.findAll(searchQuery, sort) }
                ?: teacherProfileEntityRepository.findAll(sort)
            ).map {
                val subscription =
                    teacherSubscriptionService.buildSubscriptionDto(it)
                TeacherProfileAdminView.fromEntity(it, subscription)
            }
    }

    override fun listForAdmin(excludeIds: List<UUID>, sort: Sort): List<TeacherProfileAdminView> {
        val teacherEntities = if (excludeIds.isEmpty()) {
            teacherProfileEntityRepository.findAll(sort)
        } else {
            teacherProfileEntityRepository.findAllByIdNotIn(excludeIds, sort)
        }
        return teacherEntities.map {
            val subscription =
                teacherSubscriptionService.buildSubscriptionDto(it)
            TeacherProfileAdminView.fromEntity(it, subscription)
        }
    }

    override fun getTeacherCredentialsByEmail(email: String): TeacherCredentials? {
        return teacherProfileEntityRepository.findByEmail(email)
            ?.let(TeacherCredentials.Companion::fromEntity)
    }

    override fun getTeacherCredentialsById(teacherId: UUID): TeacherCredentials? {
        return teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.let(TeacherCredentials.Companion::fromEntity)
    }

    override fun exists(email: String): Boolean {
        return teacherProfileEntityRepository.existsByEmail(email)
    }

    override fun exists(teacherId: UUID): Boolean {
        return teacherProfileEntityRepository.existsById(teacherId)
    }

    override fun setPassword(email: String, newPassword: String) {
        teacherProfileEntityRepository.findByEmail(email)
            ?.let {
                it.passwordHash = passwordEncoder.encode(newPassword)
                teacherProfileEntityRepository.save(it)
            }
    }

    override fun delete(teacherId: UUID) {
        teacherProfileEntityRepository.deleteById(teacherId)
    }
}
