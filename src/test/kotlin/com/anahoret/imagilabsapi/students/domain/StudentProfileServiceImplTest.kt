package com.anahoret.imagilabsapi.students.domain

import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@ImagiLabsTestPropertySource
@DisplayName("Student Profile Service")
class StudentProfileServiceImplTest {

    @Autowired
    lateinit var testStudentProfileServiceImpl: StudentProfileServiceImpl

    @Test
    fun `should create student password successfully`() {
        val method = testStudentProfileServiceImpl.javaClass.getDeclaredMethod("createStudentPassword")
        method.isAccessible = true
        val returnValue = method.invoke(testStudentProfileServiceImpl)
        assertEquals(8, returnValue.toString().length)
        assertEquals(returnValue.toString().uppercase(), returnValue.toString())
    }
}
