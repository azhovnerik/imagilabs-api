package com.anahoret.imagilabsapi.teachers.domain

import org.springframework.stereotype.Service
import java.util.*

interface TeacherService {

    fun createTeacher(request: TeacherSignupRequest)
    fun getTeacherById(id: UUID): TeacherProfile?
    fun getTeacherCredentialsByEmail(email: String): TeacherCredentials?
}

@Service
class TeacherServiceImpl : TeacherService {

    private val id = UUID.fromString("c7d9cd41-2032-42bd-b889-79fea135d128")
    private val mockTeacherProfile = TeacherProfile(
        id,
        "John",
        "Doe"
    )
    private val mockTeacherCredentials = TeacherCredentials(
        id,
        "teacher@example.com",
        "\$2a\$10\$/GVf6zeUfqL515E3cZ34luTuSdPLyUe0bzhSd4p97T.fWEt7LNZiO"
    )

    override fun createTeacher(request: TeacherSignupRequest) {
        TODO("not implemented")
    }

    override fun getTeacherById(id: UUID): TeacherProfile? {
        return mockTeacherProfile
    }

    override fun getTeacherCredentialsByEmail(email: String): TeacherCredentials? {
        return mockTeacherCredentials
    }

}
