package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.stereotype.Service

interface TeacherPasswordResetService {

    fun createCode(email: String): String?
    fun getCode(email: String): String?
    fun resetCode(email: String)
}

@Service
class TeacherPasswordResetServiceImpl(
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository
) : TeacherPasswordResetService {

    override fun createCode(email: String): String? {
        return teacherProfileEntityRepository.findByEmail(email)
            ?.let {
                it.passwordResetCode = createVerificationCode()
                teacherProfileEntityRepository.save(it)
                it.passwordResetCode
            }
    }

    override fun getCode(email: String): String? {
        return teacherProfileEntityRepository.findByEmail(email)?.passwordResetCode
    }

    override fun resetCode(email: String) {
        teacherProfileEntityRepository.findByEmail(email)?.let {
            it.passwordResetCode = null
            teacherProfileEntityRepository.save(it)
        }
    }

    private fun createVerificationCode(): String {
        return RandomStringUtils.randomAlphanumeric(4).lowercase()
    }

}
