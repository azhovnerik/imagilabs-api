package com.anahoret.imagilabsapi.classrooms.domain

import com.anahoret.imagilabsapi.spring.ImagiLabsTestPropertySource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@ImagiLabsTestPropertySource
@DisplayName("Classroom Service")
class ClassroomServiceImplTest {

    @Autowired
    lateinit var testClassroomServiceImpl: ClassroomServiceImpl

    @Test
    fun `should generate unique access code successfully`() {
        val method = testClassroomServiceImpl.javaClass.getDeclaredMethod("generateUniqueAccessCode")
        method.isAccessible = true
        val returnValue = method.invoke(testClassroomServiceImpl)
        assertEquals(6, returnValue.toString().length)
        assertEquals(returnValue.toString().uppercase(), returnValue.toString())
    }
}
