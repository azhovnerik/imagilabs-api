package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.lovable.storage.LovableClassroomEntity
import com.anahoret.imagilabsapi.lovable.storage.LovableClassroomEntityRepository
import org.springframework.stereotype.Service
import java.util.*

interface LovableClassroomService {
    fun enableIntegrationForClassroom(classroomId: UUID)
    fun setPausedIntegrationForClassroom(classroomId: UUID, paused: Boolean)
    fun integrationEnabledForClassroom(classroomId: UUID): Boolean
    fun integrationPausedForClassroom(classroomId: UUID): Boolean
    fun deleteForClassroom(classroomId: UUID)
    fun getIntegrationForClassroom(classroomId: UUID): LovableClassroom?
}

@Service
class LovableClassroomServiceImpl(
    private val lovableClassroomEntityRepository: LovableClassroomEntityRepository
) : LovableClassroomService {

    override fun enableIntegrationForClassroom(classroomId: UUID) {
        lovableClassroomEntityRepository.findOneByClassroomId(classroomId) ?: run {
            lovableClassroomEntityRepository.save(LovableClassroomEntity(classroomId))
        }
    }

    override fun setPausedIntegrationForClassroom(classroomId: UUID, paused: Boolean) {
        lovableClassroomEntityRepository.findOneByClassroomId(classroomId)?.let {
            it.lovableIntegrationPaused = paused
            lovableClassroomEntityRepository.save(it)
        }
    }

    override fun integrationEnabledForClassroom(classroomId: UUID): Boolean {
        return lovableClassroomEntityRepository.findOneByClassroomId(classroomId)
            ?.lovableIntegrationEnabled ?: false
    }

    override fun integrationPausedForClassroom(classroomId: UUID): Boolean {
        return lovableClassroomEntityRepository.findOneByClassroomId(classroomId)
            ?.lovableIntegrationPaused ?: false
    }

    override fun deleteForClassroom(classroomId: UUID) {
        lovableClassroomEntityRepository.deleteByClassroomId(classroomId)
    }

    override fun getIntegrationForClassroom(classroomId: UUID): LovableClassroom? {
        return lovableClassroomEntityRepository.getByClassroomId(classroomId)?.let {
            LovableClassroom(it.classroomId, it.lovableIntegrationEnabled, it.lovableIntegrationPaused)
        }
    }

}
