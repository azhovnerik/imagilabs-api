package com.anahoret.imagilabsapi.edlink.domain

import com.anahoret.imagilabsapi.edlink.storage.EdLinkOAuthStateEntity
import com.anahoret.imagilabsapi.edlink.storage.EdLinkOAuthStateRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

interface EdLinkOAuthStateService {
    fun create(): UUID
    fun exists(id: UUID): Boolean
    fun delete(id: UUID)
    fun cleanup()
}

@Service
@Transactional
class EdLinkOAuthStateServiceImpl(
    private val edLinkOAuthRepository: EdLinkOAuthStateRepository
) : EdLinkOAuthStateService {

    override fun create(): UUID {
        val entity = EdLinkOAuthStateEntity()
        val savedEntity = edLinkOAuthRepository.save(entity)
        return savedEntity.id!!
    }

    @Transactional(readOnly = true)
    override fun exists(id: UUID): Boolean {
        return edLinkOAuthRepository.existsById(id)
    }

    override fun delete(id: UUID) {
        edLinkOAuthRepository.deleteById(id)
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    override fun cleanup() {
        val oneHourAgo = System.currentTimeMillis() - 1.hours.inWholeMilliseconds
        edLinkOAuthRepository.deleteByCreatedAtLessThan(oneHourAgo)
    }
}
