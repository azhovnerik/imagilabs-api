package com.anahoret.imagilabsapi.classrooms.domain.studentcredentialscards

import com.anahoret.imagilabsapi.students.domain.StudentClassroomCard
import org.springframework.stereotype.Service
import java.io.InputStream

interface StudentCredentialsCardsCsvGenerator {

    fun generate(studentClassroomCards: List<StudentClassroomCard>): InputStream
}

@Service
class StudentCredentialsCardsCsvGeneratorImpl : StudentCredentialsCardsCsvGenerator {

    override fun generate(studentClassroomCards: List<StudentClassroomCard>): InputStream {
        TODO("not implemented")
    }

}
