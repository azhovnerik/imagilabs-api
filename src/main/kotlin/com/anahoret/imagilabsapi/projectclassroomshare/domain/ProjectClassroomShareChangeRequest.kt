package com.anahoret.imagilabsapi.projectclassroomshare.domain

import java.util.*

class ProjectClassroomShareChangeRequest(
    val projectId: UUID,
    val classroomIds: List<UUID>
)
