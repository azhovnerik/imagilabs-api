package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

interface TipTokensService {
    fun getStudentTipTokens(studentId: UUID): Int?
    fun replenishTipTokens()
    fun withdrawOneTipToken(studentId: UUID)
    fun hasTipTokens(studentId: UUID): Boolean?
}

@Service
class TipTokensServiceImpl(
    private val studentProfileEntityRepository: StudentProfileEntityRepository,
    @Value("\${spring.ai.openai.tip-tokens-per-hour}") private val tipTokens: Int
) : TipTokensService {

    override fun getStudentTipTokens(studentId: UUID): Int? {
        return studentProfileEntityRepository.findByIdOrNull(studentId)?.tipTokens
    }

    override fun replenishTipTokens() {
        val updatedEntities = studentProfileEntityRepository.findAll()
            .map {
                it.tipTokens = tipTokens
                it
            }
        studentProfileEntityRepository.saveAll(updatedEntities)
    }

    override fun withdrawOneTipToken(studentId: UUID) {
        studentProfileEntityRepository.findByIdOrNull(studentId)?.let {
            it.tipTokens -= 1
            studentProfileEntityRepository.save(it)
        }
    }

    override fun hasTipTokens(studentId: UUID): Boolean? {
        return studentProfileEntityRepository.findByIdOrNull(studentId)?.tipTokens?.let { it > 0 }
    }
}
