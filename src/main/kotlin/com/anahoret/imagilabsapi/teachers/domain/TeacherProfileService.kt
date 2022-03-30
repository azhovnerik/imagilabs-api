package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

interface TeacherProfileService {

    fun createTeacher(request: TeacherSignupRequest): TeacherProfile
    fun getTeacherById(id: UUID): TeacherProfile?
    fun getTeacherCredentialsByEmail(email: String): TeacherCredentials?
    fun exists(email: String): Boolean
    fun listByIds(ids: Iterable<UUID>): List<TeacherProfile>
    fun listAll(): List<TeacherProfile>
}

@Service
class TeacherProfileServiceImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    private val passwordEncoder: PasswordEncoder
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
                    howDidYouHearAboutUs
                )
            ).let(TeacherProfile.Companion::fromEntity)
        }
    }

    override fun getTeacherById(id: UUID): TeacherProfile? {
        return teacherProfileEntityRepository.findByIdOrNull(id)
            ?.let(TeacherProfile.Companion::fromEntity)
    }

    override fun listByIds(ids: Iterable<UUID>): List<TeacherProfile> {
        return teacherProfileEntityRepository.findAllById(ids)
            .map(TeacherProfile.Companion::fromEntity)
    }

    override fun listAll(): List<TeacherProfile> {
        return teacherProfileEntityRepository.findAll()
            .map(TeacherProfile.Companion::fromEntity)
    }

    override fun getTeacherCredentialsByEmail(email: String): TeacherCredentials? {
        return teacherProfileEntityRepository.findByEmail(email)
            ?.let(TeacherCredentials.Companion::fromEntity)
    }

    override fun exists(email: String): Boolean {
        return teacherProfileEntityRepository.existsByEmail(email)
    }

}
