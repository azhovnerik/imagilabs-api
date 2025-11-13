package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.classrooms.domain.StudentsCreatedEvent
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class StudentsCreatedInitLovableAccountsListenerTest {

    private val connectUseCase: ConnectLovableAccountToUserUseCase = mockk(relaxed = true)
    private val lovableClassroomService: LovableClassroomService = mockk()
    private val listener = StudentsCreatedInitLovableAccountsListener(connectUseCase, lovableClassroomService)

    @Test
    fun `does nothing when integration disabled`() {
        val classroomId = UUID.randomUUID()
        every { lovableClassroomService.integrationEnabledForClassroom(classroomId) } returns false
        val s1 = StudentProfile(UUID.randomUUID(), "s1", "u1", 1L)
        val s2 = StudentProfile(UUID.randomUUID(), "s2", "u2", 1L)
        val event = StudentsCreatedEvent(listOf(s1, s2), classroomId)

        listener.onApplicationEvent(event)

        verify(exactly = 0) { connectUseCase.connect(any()) }
    }

    @Test
    fun `connects all students when integration enabled`() {
        val classroomId = UUID.randomUUID()
        every { lovableClassroomService.integrationEnabledForClassroom(classroomId) } returns true
        val s1 = StudentProfile(UUID.randomUUID(), "s1", "u1", 1L)
        val s2 = StudentProfile(UUID.randomUUID(), "s2", "u2", 1L)
        val event = StudentsCreatedEvent(listOf(s1, s2), classroomId)

        listener.onApplicationEvent(event)

        verify { connectUseCase.connect(s1) }
        verify { connectUseCase.connect(s2) }
    }
}
