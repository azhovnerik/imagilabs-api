package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.subscription.domain.TeacherSubscriptionService
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.*

interface TeacherProfileService {

    fun createTeacher(request: TeacherSignupRequest): TeacherProfile
    fun getTeacherById(id: UUID): TeacherProfile?
    fun getTeacherIdByEmail(email: String): UUID?
    fun getTeacherByIdForAdmin(id: UUID): TeacherProfileAdminView?
    fun getTeachersByIdsForAdmin(ids: List<UUID>): List<TeacherProfileAdminView?>
    fun getTeacherCredentialsByEmail(email: String): TeacherCredentials?
    fun getTeacherCredentialsById(teacherId: UUID): TeacherCredentials?
    fun exists(email: String): Boolean
    fun exists(teacherId: UUID): Boolean
    fun listByIds(ids: Iterable<UUID>): List<TeacherProfile>
    fun listAllForAdmin(searchQuery: String?, sort: Sort): List<TeacherProfileAdminView>
    fun listForAdmin(excludeIds: List<UUID>, sort: Sort): List<TeacherProfileAdminView>
    fun setPassword(email: String, newPassword: String)
    fun getTeachersWithExpiredSubscriptionBetween(leftRange: Long, rightRange: Long): List<TeacherProfile>
    fun delete(teacherId: UUID)
    fun completeChatOnboarding(teacherId: UUID): TeacherProfile?
    fun isAiChatOnboardingCompleted(teacherId: UUID): Boolean
    fun setIntroSeen(teacherId: UUID): TeacherProfile?
    fun isAiChatIntroSeen(teacherId: UUID): Boolean
    fun listTeachersByStudent(studentId: UUID): List<TeacherProfile>
    fun getByEdLink(edLinkIntegrationId: UUID, edLinkPersonId: UUID): TeacherProfile?
    fun listAllByEdLink(): List<TeacherProfile>
}

@Service
class TeacherProfileServiceImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    private val passwordEncoder: PasswordEncoder,
    private val teacherSubscriptionService: TeacherSubscriptionService,
    private val clock: Clock,
    @param:Value($$"${spring.ai.openai.tip-tokens-per-hour}") private val tipTokens: Int
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
                    tipTokens,
                    tipTokensReplenishedAt = clock.millis(),
                    edLinkIntegrationId = edLinkIntegrationId,
                    edLinkPersonId = edLinkPersonId
                )
            ).let(::toTeacherProfile)
        }
    }

    override fun getTeacherById(id: UUID): TeacherProfile? {
        return teacherProfileEntityRepository.findByIdOrNull(id)
            ?.let(::toTeacherProfile)
    }

    override fun getTeacherIdByEmail(email: String): UUID? {
        return teacherProfileEntityRepository.findByEmail(email)?.id
    }

    override fun getTeacherByIdForAdmin(id: UUID): TeacherProfileAdminView? {
        return teacherProfileEntityRepository.findByIdOrNull(id)
            ?.let {
                val subscription =
                    teacherSubscriptionService.buildSubscriptionDto(it.id!!, it)
                TeacherProfileAdminView.fromEntity(it, subscription)
            }
    }

    override fun getTeachersByIdsForAdmin(ids: List<UUID>): List<TeacherProfileAdminView> {
        return teacherProfileEntityRepository.findAllById(ids)
            .map {
                val subscription =
                    teacherSubscriptionService.buildSubscriptionDto(it.id!!, it)
                TeacherProfileAdminView.fromEntity(it, subscription)
            }
    }

    override fun listByIds(ids: Iterable<UUID>): List<TeacherProfile> {
        return teacherProfileEntityRepository.findAllById(ids)
            .map(::toTeacherProfile)
    }

    override fun listAllForAdmin(searchQuery: String?, sort: Sort): List<TeacherProfileAdminView> {
        return (
                searchQuery?.let { teacherProfileEntityRepository.findAll(searchQuery, sort) }
                    ?: teacherProfileEntityRepository.findAll(sort)
                ).map {
                val subscription =
                    teacherSubscriptionService.buildSubscriptionDto(it.id!!, it)
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
                teacherSubscriptionService.buildSubscriptionDto(it.id!!, it)
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

    override fun getTeachersWithExpiredSubscriptionBetween(leftRange: Long, rightRange: Long): List<TeacherProfile> {
        return teacherProfileEntityRepository
            .findAllBySubscriptionStartIsNotNullAndSubscriptionEndBetween(leftRange, rightRange)
            .map(::toTeacherProfile)
    }

    override fun delete(teacherId: UUID) {
        teacherProfileEntityRepository.deleteById(teacherId)
    }

    override fun completeChatOnboarding(teacherId: UUID): TeacherProfile? {
        return teacherProfileEntityRepository.findByIdOrNull(teacherId)?.let {
            it.aiChatOnboardingCompleted = true
            teacherProfileEntityRepository.save(it)
        }?.let { TeacherProfile.fromEntity(it, teacherSubscriptionService.buildSubscriptionDto(it.id!!, it)) }
    }

    override fun setIntroSeen(teacherId: UUID): TeacherProfile? {
        return teacherProfileEntityRepository.findByIdOrNull(teacherId)?.let {
            it.aiChatIntroSeen = true
            teacherProfileEntityRepository.save(it)
        }?.let { TeacherProfile.fromEntity(it, teacherSubscriptionService.buildSubscriptionDto(it.id!!, it)) }
    }

    override fun isAiChatOnboardingCompleted(teacherId: UUID): Boolean {
        return teacherProfileEntityRepository.isAiChatOnboardingCompleted(teacherId)
    }

    override fun isAiChatIntroSeen(teacherId: UUID): Boolean {
        return teacherProfileEntityRepository.isAiChatIntroSeen(teacherId)
    }

    override fun listTeachersByStudent(studentId: UUID): List<TeacherProfile> {
        val teacherProfileEntity = teacherProfileEntityRepository.listByStudentId(studentId)
        return teacherProfileEntity.map(::toTeacherProfile)
    }

    override fun getByEdLink(edLinkIntegrationId: UUID, edLinkPersonId: UUID): TeacherProfile? {
        return teacherProfileEntityRepository
            .findOneByEdLinkIntegrationIdAndEdLinkPersonId(edLinkIntegrationId, edLinkPersonId)
            ?.let(::toTeacherProfile)
    }

    override fun listAllByEdLink(): List<TeacherProfile> {
        return teacherProfileEntityRepository.findAllByEdLink()
            .map(::toTeacherProfile)
    }

    private fun toTeacherProfile(entity: TeacherProfileEntity): TeacherProfile {
        val subscription = teacherSubscriptionService.buildSubscriptionDto(entity.id!!, entity)
        val schools = entity.schools.map { com.anahoret.imagilabsapi.schools.domain.School.fromEntity(it) }
        return TeacherProfile.fromEntity(entity, subscription, schools)
    }
}
