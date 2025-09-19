package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.classrooms.domain.StudentsCreatedEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class StudentsCreatedInitLovableAccountsListener(
    private val connectLovableAccountToUserUseCase: ConnectLovableAccountToUserUseCase,
    private val lovableClassroomService: LovableClassroomService
) : ApplicationListener<StudentsCreatedEvent> {

    override fun onApplicationEvent(event: StudentsCreatedEvent) {
        if (!lovableClassroomService.integrationEnabledForClassroom(event.classroomId)) return
        event.newStudents.forEach { connectLovableAccountToUserUseCase.connect(it) }
    }

}
