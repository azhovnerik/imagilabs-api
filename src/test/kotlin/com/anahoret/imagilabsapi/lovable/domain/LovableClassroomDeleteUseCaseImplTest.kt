package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.userclassroomlink.domain.StudentClassroomLinkService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class LovableClassroomDeleteUseCaseImplTest {

    private val studentProfileService: StudentProfileService = mockk()
    private val lovableAccountService: LovableAccountService = mockk(relaxed = true)
    private val lovableClassroomService: LovableClassroomService = mockk(relaxed = true)
    private val studentClassroomLinkService: StudentClassroomLinkService = mockk()

    private val useCase: LovableClassroomDeleteUseCase = LovableClassroomDeleteUseCaseImpl(
        lovableAccountService,
        lovableClassroomService,
        studentClassroomLinkService
    )

    @Test
    fun `delete removes lovable accounts for students in classroom and classroom integration record`() {
        val classroomId = UUID.randomUUID()
        val student1 = StudentProfile(UUID.randomUUID(), "Alice", "alice1", 1L)
        val student2 = StudentProfile(UUID.randomUUID(), "Bob", "bob2", 2L)

        every { studentClassroomLinkService.listStudentsByClassroom(classroomId) } returns listOf(student1, student2)

        useCase.delete(classroomId)

        verify {
            lovableAccountService.deleteByIds(match {
                it.containsAll(
                    listOf(
                        student1.id,
                        student2.id
                    )
                ) && it.size == 2
            })
        }
        verify { lovableClassroomService.deleteForClassroom(classroomId) }
        verify { studentClassroomLinkService.listStudentsByClassroom(classroomId) }
    }

    @Test
    fun `delete handles empty classroom gracefully`() {
        val classroomId = UUID.randomUUID()
        every { studentClassroomLinkService.listStudentsByClassroom(classroomId) } returns emptyList()

        useCase.delete(classroomId)

        verify { studentClassroomLinkService.listStudentsByClassroom(classroomId) }
        // deleteByIds should be called with empty list which should be handled inside service; but here we can simply ensure it is called once.
        verify { lovableAccountService.deleteByIds(match { it.isEmpty() }) }
        verify { lovableClassroomService.deleteForClassroom(classroomId) }
    }
}
