package com.anahoret.imagilabsapi.classrooms.domain

import com.fasterxml.jackson.annotation.JsonIgnore

class ClassroomCreateRequest(
    val name: String,
    studentNames: String
) {

    @JsonIgnore
    val studentCreateRequests = studentNames
        .split(",", "\n")
        .map(String::trim)
        .filter(String::isNotBlank)
        .map(::StudentCreateRequest)

    class StudentCreateRequest(val name: String)
}
