package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import org.springframework.stereotype.Service
import java.util.*

interface LovableClassroomDeleteUseCase {
    fun delete(classroomId: UUID)
}

@Service
class LovableClassroomDeleteUseCaseImpl(
    private val lovableAccountService: LovableAccountService,
    private val lovableClassroomService: LovableClassroomService,
    private val studentClassroomLinkService: StudentClassroomLinkService
) : LovableClassroomDeleteUseCase {
    override fun delete(classroomId: UUID) {
        val studentIds = studentClassroomLinkService.listStudentsByClassroom(classroomId).map(StudentProfile::id)
        lovableAccountService.deleteByIds(studentIds)
        lovableClassroomService.deleteForClassroom(classroomId)
    }

}
