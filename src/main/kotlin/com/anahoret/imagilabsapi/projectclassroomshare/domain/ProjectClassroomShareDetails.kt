package com.anahoret.imagilabsapi.projectclassroomshare.domain

import com.anahoret.imagilabsapi.classrooms.domain.Classroom
import java.util.*

@Suppress("unused")
class ProjectClassroomShareDetails(
    val classroomId: UUID,
    val classroomName: String
) {

    companion object {

        fun fromClassroom(classroom: Classroom): ProjectClassroomShareDetails {
            return with(classroom) {
                ProjectClassroomShareDetails(id, name)
            }
        }
    }
}
