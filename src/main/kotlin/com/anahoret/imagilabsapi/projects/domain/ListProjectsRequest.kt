package com.anahoret.imagilabsapi.projects.domain

import java.util.*

class ListProjectsRequest(
    val ownerId: UUID?,
    val state: ProjectState?,
    val sharedInClassesIds: Set<UUID>?
)
