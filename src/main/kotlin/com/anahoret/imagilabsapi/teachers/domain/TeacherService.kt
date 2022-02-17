package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntity
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

interface TeacherService {

    fun createTeacher(request: TeacherSignupRequest)
    fun getTeacherById(id: UUID): TeacherProfile?
    fun getTeacherCredentialsByEmail(email: String): TeacherCredentials?
}

@Service
class TeacherServiceImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    private val passwordEncoder: PasswordEncoder
) : TeacherService {

    override fun createTeacher(request: TeacherSignupRequest) {
        with(request) {
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
            )
        }
    }

    override fun getTeacherById(id: UUID): TeacherProfile? {
        return teacherProfileEntityRepository.findByIdOrNull(id)
            ?.let(TeacherProfile.Companion::fromEntity)
    }

    override fun getTeacherCredentialsByEmail(email: String): TeacherCredentials? {
        return teacherProfileEntityRepository.findByEmail(email)
            ?.let(TeacherCredentials.Companion::fromEntity)
    }

}
