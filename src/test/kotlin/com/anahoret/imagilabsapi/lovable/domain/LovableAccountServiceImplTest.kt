package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.common.testAdmin
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.lovable.storage.LovableAccountEntity
import com.anahoret.imagilabsapi.lovable.storage.LovableAccountProjection
import com.anahoret.imagilabsapi.lovable.storage.LovableAccountRepository
import com.anahoret.imagilabsapi.users.UserType
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.util.*

class LovableAccountServiceImplTest {

    private lateinit var repo: LovableAccountRepository
    private lateinit var clock: Clock
    private lateinit var service: LovableAccountService

    @BeforeEach
    fun setUp() {
        repo = mockk(relaxed = true)
        clock = mockk()
        service = LovableAccountServiceImpl(repo, clock)
    }

    @Test
    fun `connectedCount delegates to repository`() {
        val id = UUID.randomUUID()
        every { repo.countByConnectedUser(id) } returns 5

        val result = service.connectedCount(id)

        assertEquals(5, result)
        verify(exactly = 1) { repo.countByConnectedUser(id) }
    }

    @Test
    fun `getActive returns mapped domain when entity exists`() {
        val teacher = testTeacher()
        val entity = LovableAccountProjectionTestImpl(teacher.id, null, "a@x.com", "secret")
        every { repo.findOneByConnectedUserAndActiveTrue(teacher.id) } returns entity

        val result = service.getActive(teacher)

        assertNotNull(result)
        assertEquals("a@x.com", result!!.email)
        assertEquals("secret", result.password)
        verify { repo.findOneByConnectedUserAndActiveTrue(teacher.id) }
    }

    @Test
    fun `getActive returns null when no active entity`() {
        val teacher = testTeacher()
        every { repo.findOneByConnectedUserAndActiveTrue(teacher.id) } returns null

        val result = service.getActive(teacher)

        assertNull(result)
        verify { repo.findOneByConnectedUserAndActiveTrue(teacher.id) }
    }

    @Test
    fun `connectToUser throws on admin`() {
        val admin = testAdmin()
        assertEquals(UserType.ADMIN, admin.userType)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            service.connectToUser(admin)
        }
        assertTrue(ex.message!!.contains("Cannot connect admin"))
    }

    @Test
    fun `connectToUser returns null when no free accounts available`() {
        val teacher = testTeacher()
        val alreadyConnected = listOf(
            LovableAccountEntity("e1@x.com", "p1", teacher.id, 10L, active = false),
            LovableAccountEntity("e2@x.com", "p2", teacher.id, 20L, active = true)
        )

        every { repo.findByConnectedUser(teacher.id) } returns alreadyConnected
        every { repo.saveAll(any<List<LovableAccountEntity>>()) } answers { firstArg() }
        every { repo.findFirstByConnectedUserIsNull() } returns null

        val result = service.connectToUser(teacher)
        assertNull(result)

        verify(inverse = true) { repo.findByConnectedUser(teacher.id) }
        verify(inverse = true) { repo.saveAll(alreadyConnected) }

        verify { repo.findFirstByConnectedUserIsNull() }
        confirmVerified(repo)
    }

    @Test
    fun `connectToUser activates and returns next free account`() {
        val teacher = testTeacher()
        val previouslyConnected = listOf(
            LovableAccountEntity("old@x.com", "oldpass", teacher.id, 5L, active = true)
        )
        val free = LovableAccountEntity("new@x.com", "newpass", null, null, active = false)

        every { clock.millis() } returns 12345L
        every { repo.findByConnectedUser(teacher.id) } returns previouslyConnected
        every { repo.saveAll(any<List<LovableAccountEntity>>()) } answers { firstArg() }
        every { repo.findFirstByConnectedUserIsNull() } returns free
        every { repo.save(any<LovableAccountEntity>()) } answers { firstArg() }
        every { repo.findOneByConnectedUserAndActiveTrue(teacher.id) } returns LovableAccountProjectionTestImpl(
            teacher.id, null, "new@x.com", "newpass"
        )

        val result = service.connectToUser(teacher)

        assertNotNull(result)
        assertEquals("new@x.com", result!!.email)
        assertEquals("newpass", result.password)

        // verify state changes on the entity that got connected
        assertEquals(teacher.id, free.connectedUser)
        assertEquals(12345L, free.connectedAt)
        assertTrue(free.active)

        verifySequence {
            repo.findFirstByConnectedUserIsNull()
            repo.findByConnectedUser(teacher.id)
            repo.saveAll(match<List<LovableAccountEntity>> { list -> list.all { !it.active } })
            repo.save(free)
            repo.findOneByConnectedUserAndActiveTrue(teacher.id)
        }
        confirmVerified(repo)
    }

    @Test
    fun `getByConnectedUsers returns empty list when input is empty`() {
        val result = service.getByConnectedUsers(emptyList())
        assertTrue(result.isEmpty())
        confirmVerified(repo)
    }

    @Test
    fun `getByConnectedUsers maps entities to domain`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val entities = listOf(
            LovableAccountProjectionTestImpl(id1, null, "u1@x.com", "p1"),
            LovableAccountProjectionTestImpl(id2, null, "u2@x.com", "p2")
        )

        every { repo.findByConnectedUserIn(match { it.containsAll(listOf(id1, id2)) }) } returns entities

        val result = service.getByConnectedUsers(listOf(id1, id2))

        assertEquals(2, result.size)
        assertEquals("u1@x.com", result[0].email)
        assertEquals("p1", result[0].password)
        assertEquals(id1, result[0].connectedUserId)
        assertEquals("u2@x.com", result[1].email)
        assertEquals("p2", result[1].password)
        assertEquals(id2, result[1].connectedUserId)

        verify { repo.findByConnectedUserIn(match { it.containsAll(listOf(id1, id2)) }) }
        confirmVerified(repo)
    }

    @Test
    fun `deleteByIds does nothing when input list is empty`() {
        service.deleteByIds(emptyList())
        verify(exactly = 0) { repo.deleteByConnectedUserIn(any()) }
        confirmVerified(repo)
    }

    @Test
    fun `deleteByIds delegates to repository with provided ids`() {
        val ids = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())

        every { repo.deleteByConnectedUserIn(ids) } just Runs

        service.deleteByIds(ids)

        verify(exactly = 1) { repo.deleteByConnectedUserIn(match { it == ids }) }
        confirmVerified(repo)
    }

    private class LovableAccountProjectionTestImpl(
        override val connectedUser: UUID?,
        override val username: String?,
        override val email: String,
        override val password: String
    ) : LovableAccountProjection
}
