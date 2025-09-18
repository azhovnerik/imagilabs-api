package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.students.domain.StudentProfile
import org.springframework.context.ApplicationEvent
import java.util.*

class StudentsCreatedEvent(
    val newStudents: List<StudentProfile>,
    val classroomId: UUID
) : ApplicationEvent(Unit)
