package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import org.springframework.stereotype.Service
import java.util.*

interface LovableClassroomDeleteUseCase {
    fun delete(classroomId: UUID)
}

@Service
class LovableClassroomDeleteUseCaseImpl(
    private val studentProfileService: StudentProfileService,
    private val lovableAccountService: LovableAccountService,
    private val lovableClassroomService: LovableClassroomService,
) : LovableClassroomDeleteUseCase {
    override fun delete(classroomId: UUID) {
        val studentIds = studentProfileService.listByClassroom(classroomId).map(StudentProfile::id)
        lovableAccountService.deleteByIds(studentIds)
        lovableClassroomService.deleteForClassroom(classroomId)
    }

}
