package com.anahoret.imagilabsapi.signup.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

interface TeacherEmailVerificationService {

    fun getTeacherVerificationCode(teacherId: UUID): String?
    fun generateNewVerificationCode(teacherId: UUID): String?
    fun setEmailVerified(teacherId: UUID)
}

@Service
class TeacherEmailVerificationServiceImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository
) : TeacherEmailVerificationService {

    override fun getTeacherVerificationCode(teacherId: UUID): String? {
        return teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.emailVerificationCode
    }

    override fun generateNewVerificationCode(teacherId: UUID): String? {
        return teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.let {
                it.emailVerificationCode = RandomStringUtils.randomAlphanumeric(4).lowercase()
                teacherProfileEntityRepository.save(it)
            }?.emailVerificationCode
    }

    override fun setEmailVerified(teacherId: UUID) {
        teacherProfileEntityRepository.findByIdOrNull(teacherId)
            ?.let {
                it.emailVerificationCode = null
                it.emailVerified = true
                teacherProfileEntityRepository.save(it)
            }
    }

}


