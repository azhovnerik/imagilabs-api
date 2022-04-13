package com.anahoret.imagilabsapi.teachers.domain

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
    fun getTeacherCredentialsByEmail(email: String): TeacherCredentials?
    fun getTeacherCredentialsById(teacherId: UUID): TeacherCredentials?
    fun exists(email: String): Boolean
    fun exists(teacherId: UUID): Boolean
    fun listByIds(ids: Iterable<UUID>): List<TeacherProfile>
    fun listAll(searchQuery: String?, sort: Sort): List<TeacherProfile>
    fun setPassword(email: String, newPassword: String)
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

    override fun listAll(searchQuery: String?, sort: Sort): List<TeacherProfile> {
        return (
            searchQuery?.let { teacherProfileEntityRepository.findAll(searchQuery, sort) }
                ?: teacherProfileEntityRepository.findAll(sort)
            ).map(TeacherProfile.Companion::fromEntity)
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

}
