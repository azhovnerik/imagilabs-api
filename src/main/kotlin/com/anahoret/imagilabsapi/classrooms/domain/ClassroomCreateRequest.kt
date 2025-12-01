package com.anahoret.imagilabsapi.classrooms.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

class ClassroomCreateRequest(
    val name: String,
    studentNames: String,
    val edLinkIntegrationId: UUID?,
    val edLinkClassId: UUID?
) {

    @JsonIgnore
    val studentCreateRequests = StudentListParser.parse(studentNames)

}
