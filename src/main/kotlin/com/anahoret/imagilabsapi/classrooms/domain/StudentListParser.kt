package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.students.domain.StudentCreateRequest

object StudentListParser {

    fun parse(studentNames: String): List<StudentCreateRequest> {
        return studentNames
            .split(",", "\n")
            .map(String::trim)
            .filter(String::isNotBlank)
            .map(::StudentCreateRequest)
    }

}
