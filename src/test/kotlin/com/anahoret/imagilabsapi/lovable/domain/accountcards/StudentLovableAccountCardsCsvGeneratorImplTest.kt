package com.anahoret.imagilabsapi.lovable.domain.accountcards

import com.anahoret.imagilabsapi.lovable.domain.LovableAccount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class StudentLovableAccountCardsCsvGeneratorImplTest {

    private val csvGenerator = StudentLovableAccountCardsCsvGeneratorImpl()

    @Test
    fun `should generate CSV with headers for empty list`() {
        val result = csvGenerator.generate(emptyList())

        val csvContent = result.bufferedReader().readText()
        val lines = csvContent.lines()

        assertEquals(2, lines.size) // header + empty line
        assertEquals("Username,Email,Password", lines[0])
        assertEquals("", lines[1])
    }

    @Test
    fun `should generate CSV with single account`() {
        val account = LovableAccount(
            connectedUserId = UUID.randomUUID(),
            connectedStudentName = "student",
            username = "testuser",
            email = "test@example.com",
            password = "testpass123"
        )

        val result = csvGenerator.generate(listOf(account))

        val csvContent = result.bufferedReader().readText()
        val lines = csvContent.lines()

        assertEquals(3, lines.size) // header + data + empty line
        assertEquals("Username,Email,Password", lines[0])
        assertEquals("testuser,test@example.com,testpass123", lines[1])
        assertEquals("", lines[2])
    }

    @Test
    fun `should generate CSV with multiple accounts`() {
        val accounts = listOf(
            LovableAccount(
                connectedUserId = UUID.randomUUID(),
                connectedStudentName = "student1",
                username = "user1",
                email = "user1@example.com",
                password = "pass1"
            ),
            LovableAccount(
                connectedUserId = UUID.randomUUID(),
                connectedStudentName = "student2",
                username = "user2",
                email = "user2@example.com",
                password = "pass2"
            )
        )

        val result = csvGenerator.generate(accounts)

        val csvContent = result.bufferedReader().readText()
        val lines = csvContent.lines()

        assertEquals(4, lines.size) // header + 2 data + empty line
        assertEquals("Username,Email,Password", lines[0])
        assertEquals("user1,user1@example.com,pass1", lines[1])
        assertEquals("user2,user2@example.com,pass2", lines[2])
        assertEquals("", lines[3])
    }

    @Test
    fun `should handle null username correctly`() {
        val account = LovableAccount(
            connectedUserId = UUID.randomUUID(),
            connectedStudentName = "student",
            username = null,
            email = "test@example.com",
            password = "testpass123"
        )

        val result = csvGenerator.generate(listOf(account))

        val csvContent = result.bufferedReader().readText()
        val lines = csvContent.lines()

        assertEquals(3, lines.size)
        assertEquals("Username,Email,Password", lines[0])
        assertEquals("null,test@example.com,testpass123", lines[1])
    }

    @Test
    fun `should verify headers constant`() {
        val expectedHeaders = listOf("Username", "Email", "Password")
        assertEquals(expectedHeaders, StudentLovableAccountCardsCsvGeneratorImpl.HEADERS)
    }

    @Test
    fun `should return InputStream that can be read multiple times`() {
        val account = LovableAccount(
            connectedUserId = UUID.randomUUID(),
            connectedStudentName = "student",
            username = "testuser",
            email = "test@example.com",
            password = "testpass123"
        )

        val result = csvGenerator.generate(listOf(account))

        // First read
        val firstRead = result.bufferedReader().readText()
        assertTrue(firstRead.contains("Username,Email,Password"))
        assertTrue(firstRead.contains("testuser,test@example.com,testpass123"))

        // Reset and read again
        result.reset()
        val secondRead = result.bufferedReader().readText()
        assertEquals(firstRead, secondRead)
    }
}
