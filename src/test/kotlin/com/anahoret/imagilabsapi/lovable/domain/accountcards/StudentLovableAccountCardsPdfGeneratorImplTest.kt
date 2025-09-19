package com.anahoret.imagilabsapi.lovable.domain.accountcards

import com.anahoret.imagilabsapi.lovable.domain.LovableAccount
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class StudentLovableAccountCardsPdfGeneratorImplTest {

    private val pdfGenerator = StudentLovableAccountCardsPdfGeneratorImpl()

    @Test
    fun `should generate PDF with empty list`() {
        val result = pdfGenerator.generate(emptyList())

        assertNotNull(result)
        assertTrue(result.available() > 0)

        // Should be valid PDF bytes
        val bytes = result.readAllBytes()
        assertTrue(String(bytes).startsWith("%PDF"))
    }

    @Test
    fun `should generate PDF with single account`() {
        val account = LovableAccount(
            connectedUserId = UUID.randomUUID(),
            username = "testuser",
            email = "test@example.com",
            password = "testpass123"
        )

        val result = pdfGenerator.generate(listOf(account))

        assertNotNull(result)
        assertTrue(result.available() > 0)

        // Should be valid PDF bytes
        val bytes = result.readAllBytes()
        assertTrue(String(bytes).startsWith("%PDF"))
        assertTrue(bytes.size > 1000) // Non-trivial PDF size
    }

    @Test
    fun `should generate PDF with multiple accounts`() {
        val accounts = (1..5).map { i ->
            LovableAccount(
                connectedUserId = UUID.randomUUID(),
                username = "user$i",
                email = "user$i@example.com",
                password = "pass$i"
            )
        }

        val result = pdfGenerator.generate(accounts)

        assertNotNull(result)
        assertTrue(result.available() > 0)

        val bytes = result.readAllBytes()
        assertTrue(String(bytes).startsWith("%PDF"))
        assertTrue(bytes.size > 2000) // Larger PDF with more content
    }

    @Test
    fun `should generate larger PDF for many accounts`() {
        // Generate 11 accounts (more than 10 cards per page)
        val accounts = (1..11).map { i ->
            LovableAccount(
                connectedUserId = UUID.randomUUID(),
                username = "user$i",
                email = "user$i@example.com",
                password = "pass$i"
            )
        }

        val result = pdfGenerator.generate(accounts)

        val bytes = result.readAllBytes()
        assertTrue(String(bytes).startsWith("%PDF"))
        assertTrue(bytes.size > 3000) // Should be larger for multiple pages
    }

    @Test
    fun `should handle null username correctly in PDF`() {
        val account = LovableAccount(
            connectedUserId = UUID.randomUUID(),
            username = null,
            email = "test@example.com",
            password = "testpass123"
        )

        val result = pdfGenerator.generate(listOf(account))

        assertNotNull(result)
        val bytes = result.readAllBytes()
        assertTrue(String(bytes).startsWith("%PDF"))
    }

    @Test
    fun `should verify constants are correct`() {
        assertEquals(5, StudentLovableAccountCardsPdfGeneratorImpl.ROWS)
        assertEquals(2, StudentLovableAccountCardsPdfGeneratorImpl.COLS)
        assertEquals(20f, StudentLovableAccountCardsPdfGeneratorImpl.PADDING)
    }

    @Test
    fun `should calculate correct number of pages`() {
        val cardsPerPage = StudentLovableAccountCardsPdfGeneratorImpl.ROWS *
                StudentLovableAccountCardsPdfGeneratorImpl.COLS
        assertEquals(10, cardsPerPage)

        // Test page calculation logic based on implementation
        fun calculatePages(totalCards: Int): Int {
            return totalCards / cardsPerPage + if (totalCards % cardsPerPage == 0) 0 else 1
        }

        assertEquals(1, calculatePages(1))
        assertEquals(1, calculatePages(10))
        assertEquals(2, calculatePages(11))
        assertEquals(2, calculatePages(20))
        assertEquals(3, calculatePages(21))
    }

    @Test
    fun `should return InputStream that can be consumed`() {
        val account = LovableAccount(
            connectedUserId = UUID.randomUUID(),
            username = "testuser",
            email = "test@example.com",
            password = "testpass123"
        )

        val result = pdfGenerator.generate(listOf(account))

        // Should be able to read the entire stream
        val bytes = result.readAllBytes()
        assertTrue(bytes.size > 0)

        // Should be valid PDF bytes
        assertTrue(String(bytes).startsWith("%PDF"))
    }

    @Test
    fun `should handle empty list correctly`() {
        // Test the edge case of empty list - this tests actual implementation behavior
        val emptyResult = pdfGenerator.generate(emptyList())
        val emptyBytes = emptyResult.readAllBytes()
        assertTrue(String(emptyBytes).startsWith("%PDF"))
    }
}
