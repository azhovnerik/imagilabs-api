package com.anahoret.imagilabsapi.classrooms.domain

import com.fasterxml.jackson.annotation.JsonIgnore

class ClassroomUpdateRequest(
    val name: String,
    val schoolName: String?,
    studentNames: String
) {

    @JsonIgnore
    val studentCreateRequests = StudentListParser.parse(studentNames)
}
