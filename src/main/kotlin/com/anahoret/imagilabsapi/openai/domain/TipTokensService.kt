package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.common.domain.profiles.UserProfile
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import com.anahoret.imagilabsapi.teachers.storage.TeacherProfileEntityRepository
import com.anahoret.imagilabsapi.users.UserType
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

interface TipTokensService {
    fun getTipTokens(userProfile: UserProfile): Int?
    fun replenishTipTokens()
    fun withdrawOneTipToken(userProfile: UserProfile)
    fun hasTipTokens(userProfile: UserProfile): Boolean?
}

@Service
class TipTokensServiceImpl(
    private val studentProfileEntityRepository: StudentProfileEntityRepository,
    private val teacherProfileEntityRepository: TeacherProfileEntityRepository,
    @Value("\${spring.ai.openai.tip-tokens-per-hour}") private val tipTokens: Int
) : TipTokensService {

    override fun getTipTokens(userProfile: UserProfile): Int? {
        return when (userProfile.userType) {
            UserType.TEACHER -> teacherProfileEntityRepository.findByIdOrNull(userProfile.id)?.tipTokens
            UserType.STUDENT -> studentProfileEntityRepository.findByIdOrNull(userProfile.id)?.tipTokens
            UserType.ADMIN -> 0
        }
    }

    override fun replenishTipTokens() {
        val updatedTeachers = teacherProfileEntityRepository.findAll()
            .onEach { it.tipTokens = tipTokens }
        teacherProfileEntityRepository.saveAll(updatedTeachers)

        val updatedStudents = studentProfileEntityRepository.findAll()
            .onEach { it.tipTokens = tipTokens }
        studentProfileEntityRepository.saveAll(updatedStudents)
    }

    override fun withdrawOneTipToken(userProfile: UserProfile) {
        when (userProfile.userType) {
            UserType.TEACHER -> teacherProfileEntityRepository.findByIdOrNull(userProfile.id)?.let {
                it.tipTokens -= 1
                teacherProfileEntityRepository.save(it)
            }

            UserType.STUDENT -> studentProfileEntityRepository.findByIdOrNull(userProfile.id)?.let {
                it.tipTokens -= 1
                studentProfileEntityRepository.save(it)
            }

            UserType.ADMIN -> {}
        }

    }

    override fun hasTipTokens(userProfile: UserProfile): Boolean? {
        return when (userProfile.userType) {
            UserType.TEACHER -> teacherProfileEntityRepository.findByIdOrNull(userProfile.id)?.tipTokens?.let { it > 0 }
            UserType.STUDENT -> studentProfileEntityRepository.findByIdOrNull(userProfile.id)?.tipTokens?.let { it > 0 }
            UserType.ADMIN -> false
        }
    }
}
