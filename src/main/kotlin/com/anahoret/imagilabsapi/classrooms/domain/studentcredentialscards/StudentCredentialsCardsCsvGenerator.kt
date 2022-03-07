package com.anahoret.imagilabsapi.classrooms.domain.studentcredentialscards

import com.anahoret.imagilabsapi.students.domain.StudentClassroomCard
import org.springframework.stereotype.Service
import java.io.InputStream

interface StudentCredentialsCardsCsvGenerator {

    fun generate(studentClassroomCards: List<StudentClassroomCard>): InputStream
}

@Service
class StudentCredentialsCardsCsvGeneratorImpl : StudentCredentialsCardsCsvGenerator {

    companion object {

        val HEADERS = listOf(
            "Username",
            "Classroom access code",
            "Password"
        )
    }

    override fun generate(studentClassroomCards: List<StudentClassroomCard>): InputStream {
        return StringBuilder()
            .appendLine(HEADERS.joinToString(","))
            .apply {
                studentClassroomCards.forEach { card ->
                    appendLine("${card.username},${card.classroomAccessCode},${card.password}")
                }
            }.toString().byteInputStream()
    }

}
