package com.anahoret.imagilabsapi.lovable.domain

import com.anahoret.imagilabsapi.common.testAdmin
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.lovable.storage.LovableAccountEntity
import com.anahoret.imagilabsapi.lovable.storage.LovableAccountRepository
import com.anahoret.imagilabsapi.students.domain.StudentProfile
import com.anahoret.imagilabsapi.students.domain.StudentProfileService
import com.anahoret.imagilabsapi.users.UserType
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.util.*

class LovableAccountServiceImplTest {

    private lateinit var lovableAccountRepository: LovableAccountRepository
    private lateinit var clock: Clock
    private lateinit var lovableAccountService: LovableAccountService
    private val studentProfileService = mockk<StudentProfileService>()

    @BeforeEach
    fun setUp() {
        lovableAccountRepository = mockk(relaxed = true)
        clock = mockk()
        lovableAccountService = LovableAccountServiceImpl(lovableAccountRepository, studentProfileService, clock)
    }

    @Test
    fun `connectedCount delegates to repository`() {
        val id = UUID.randomUUID()
        every { lovableAccountRepository.countByConnectedUser(id) } returns 5

        val result = lovableAccountService.connectedCount(id)

        assertEquals(5, result)
        verify(exactly = 1) { lovableAccountRepository.countByConnectedUser(id) }
    }

    @Test
    fun `getActive returns mapped domain when entity exists`() {
        val teacher = testTeacher()
        val entity = LovableAccountEntity("a@x.com", "secret", "u1", teacher.id, null)
        every { lovableAccountRepository.findOneByConnectedUserAndActiveTrue(teacher.id) } returns entity

        val result = lovableAccountService.getActive(teacher)

        assertNotNull(result)
        assertEquals("a@x.com", result!!.email)
        assertEquals("secret", result.password)
        verify { lovableAccountRepository.findOneByConnectedUserAndActiveTrue(teacher.id) }
    }

    @Test
    fun `getActive returns null when no active entity`() {
        val teacher = testTeacher()
        every { lovableAccountRepository.findOneByConnectedUserAndActiveTrue(teacher.id) } returns null

        val result = lovableAccountService.getActive(teacher)

        assertNull(result)
        verify { lovableAccountRepository.findOneByConnectedUserAndActiveTrue(teacher.id) }
    }

    @Test
    fun `connectToUser throws on admin`() {
        val admin = testAdmin()
        assertEquals(UserType.ADMIN, admin.userType)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            lovableAccountService.connectToUser(admin)
        }
        assertTrue(ex.message!!.contains("Cannot connect admin"))
    }

    @Test
    fun `connectToUser returns null when no free accounts available`() {
        val teacher = testTeacher()
        val alreadyConnected = listOf(
            LovableAccountEntity("e1@x.com", "p1", "u1", teacher.id, 10L, active = false),
            LovableAccountEntity("e2@x.com", "p2", "u2", teacher.id, 20L, active = true)
        )

        every { lovableAccountRepository.findByConnectedUser(teacher.id) } returns alreadyConnected
        every { lovableAccountRepository.saveAll(any<List<LovableAccountEntity>>()) } answers { firstArg() }
        every { lovableAccountRepository.findFirstByConnectedUserIsNull() } returns null

        val result = lovableAccountService.connectToUser(teacher)
        assertNull(result)

        verify(inverse = true) { lovableAccountRepository.findByConnectedUser(teacher.id) }
        verify(inverse = true) { lovableAccountRepository.saveAll(alreadyConnected) }

        verify { lovableAccountRepository.findFirstByConnectedUserIsNull() }
        confirmVerified(lovableAccountRepository)
    }

    @Test
    fun `connectToUser activates and returns next free account`() {
        val teacher = testTeacher()
        val previouslyConnected = listOf(
            LovableAccountEntity("old@x.com", "oldpass", "u1", teacher.id, 5L, active = true)
        )
        val free = LovableAccountEntity("new@x.com", "newpass", "u1", null, null, active = false)

        every { clock.millis() } returns 12345L
        every { lovableAccountRepository.findByConnectedUser(teacher.id) } returns previouslyConnected
        every { lovableAccountRepository.saveAll(any<List<LovableAccountEntity>>()) } answers { firstArg() }
        every { lovableAccountRepository.findFirstByConnectedUserIsNull() } returns free
        every { lovableAccountRepository.save(any<LovableAccountEntity>()) } answers { firstArg() }
        every { lovableAccountRepository.findOneByConnectedUserAndActiveTrue(teacher.id) } returns LovableAccountEntity(
            "new@x.com", "newpass", "u1", teacher.id, null
        )

        val result = lovableAccountService.connectToUser(teacher)

        assertNotNull(result)
        assertEquals("new@x.com", result!!.email)
        assertEquals("newpass", result.password)

        // verify state changes on the entity that got connected
        assertEquals(teacher.id, free.connectedUser)
        assertEquals(12345L, free.connectedAt)
        assertTrue(free.active)

        verifySequence {
            lovableAccountRepository.findFirstByConnectedUserIsNull()
            lovableAccountRepository.findByConnectedUser(teacher.id)
            lovableAccountRepository.saveAll(match<List<LovableAccountEntity>> { list -> list.all { !it.active } })
            lovableAccountRepository.save(free)
            lovableAccountRepository.findOneByConnectedUserAndActiveTrue(teacher.id)
        }
        confirmVerified(lovableAccountRepository)
    }

    @Test
    fun `getByConnectedUsers returns empty list when input is empty`() {
        val result = lovableAccountService.getByConnectedUsers(emptyList())
        assertTrue(result.isEmpty())
        confirmVerified(lovableAccountRepository)
    }

    @Test
    fun `getByConnectedUsers maps entities to domain`() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val entities = listOf(
            LovableAccountEntity("u1@x.com", "p1", "u1", id1, null),
            LovableAccountEntity("u2@x.com", "p2", "u2", id2, null)
        )
        val profiles = listOf(
            mockk<StudentProfile> {
                every { id } returns id1
                every { name } returns "s1"
            },
            mockk<StudentProfile> {
                every { id } returns id2
                every { name } returns "s2"
            }
        )

        every { studentProfileService.listByIds(match { it.containsAll(listOf(id1, id2)) }) } returns profiles
        every {
            lovableAccountRepository.findByConnectedUserInAndActiveTrue(match {
                it.containsAll(
                    listOf(
                        id1,
                        id2
                    )
                )
            })
        } returns entities

        val result = lovableAccountService.getByConnectedUsers(listOf(id1, id2))

        assertEquals(2, result.size)
        assertEquals("u1@x.com", result[0].email)
        assertEquals("p1", result[0].password)
        assertEquals(id1, result[0].connectedUserId)
        assertEquals("u2@x.com", result[1].email)
        assertEquals("p2", result[1].password)
        assertEquals(id2, result[1].connectedUserId)

        verify {
            lovableAccountRepository.findByConnectedUserInAndActiveTrue(match {
                it.containsAll(
                    listOf(
                        id1,
                        id2
                    )
                )
            })
        }
        confirmVerified(lovableAccountRepository)
    }

    @Test
    fun `deleteByIds does nothing when input list is empty`() {
        lovableAccountService.deleteByIds(emptyList())
        verify(exactly = 0) { lovableAccountRepository.deleteByConnectedUserIn(any()) }
        confirmVerified(lovableAccountRepository)
    }

    @Test
    fun `deleteByIds delegates to repository with provided ids`() {
        val ids = listOf(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())

        every { lovableAccountRepository.deleteByConnectedUserIn(ids) } just Runs

        lovableAccountService.deleteByIds(ids)

        verify(exactly = 1) { lovableAccountRepository.deleteByConnectedUserIn(match { it == ids }) }
        confirmVerified(lovableAccountRepository)
    }

}
