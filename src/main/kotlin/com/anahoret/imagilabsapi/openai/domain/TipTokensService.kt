package com.anahoret.imagilabsapi.openai.domain

import com.anahoret.imagilabsapi.openai.storage.TipTokensRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface TipTokensService {
    fun refreshTipTokens()
    fun withdrawTipToken(studentId: UUID)
    fun hasTipTokens(studentId: UUID): Boolean
}

@Service
class TipTokensServiceImpl(
    private val tipTokensRepository: TipTokensRepository,
    @Value("\${spring.ai.openai.tip-tokens-per-hour}") private val tipTokens: Int
) : TipTokensService {
    @Transactional
    override fun refreshTipTokens() {
        tipTokensRepository.updateTipTokens(tipTokens)
    }

    override fun withdrawTipToken(studentId: UUID) {
        tipTokensRepository.findByIdOrNull(studentId)?.let {
            it.tipTokens -= 1
            tipTokensRepository.save(it)
        }
    }

    override fun hasTipTokens(studentId: UUID): Boolean {
        return tipTokensRepository.findByIdOrNull(studentId)
            ?.let { tipTokensRepository.hasTipTokens(studentId) }
            ?: false
    }
}
