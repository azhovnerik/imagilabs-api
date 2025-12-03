package com.anahoret.imagilabsapi.edlink.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("EdLink Filter Builder")
class EdLinkFilterBuilderTest {

    private val objectMapper = ObjectMapper()

    @Test
    fun `should build valid JSON for single field with in operator`() {
        val classId1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val classId2 = UUID.fromString("00000000-0000-0000-0000-000000000002")

        val filter = edLinkFilter {
            field("id") {
                inOperator(listOf(classId1, classId2))
            }
        }

        // Parse as JSON to verify it's valid
        val parsed = objectMapper.readValue(filter, Map::class.java)

        // Verify structure
        val idConditions = parsed["id"] as List<*>
        assertEquals(1, idConditions.size)

        val condition = idConditions[0] as Map<*, *>
        assertEquals("in", condition["operator"])
        assertEquals("$classId1,$classId2", condition["value"])
    }

    @Test
    fun `should build valid JSON for multiple fields with different operators`() {
        val personId = UUID.fromString("00000000-0000-0000-0000-000000000003")

        val filter = edLinkFilter {
            field("person_id") {
                equalsOperator(personId)
            }
            field("role") {
                equalsOperator("teacher")
            }
            field("state") {
                equalsOperator("active")
            }
        }

        println(filter)

        // Parse as JSON to verify it's valid
        val parsed = objectMapper.readValue(filter, Map::class.java)

        // Verify person_id
        val personIdConditions = parsed["person_id"] as List<*>
        assertEquals(1, personIdConditions.size)
        val personIdCondition = personIdConditions[0] as Map<*, *>
        assertEquals("eq", personIdCondition["operator"])
        assertEquals(personId.toString(), personIdCondition["value"])

        // Verify role
        val roleConditions = parsed["role"] as List<*>
        assertEquals(1, roleConditions.size)
        val roleCondition = roleConditions[0] as Map<*, *>
        assertEquals("eq", roleCondition["operator"])
        assertEquals("teacher", roleCondition["value"])

        // Verify state
        val stateConditions = parsed["state"] as List<*>
        assertEquals(1, stateConditions.size)
        val stateCondition = stateConditions[0] as Map<*, *>
        assertEquals("eq", stateCondition["operator"])
        assertEquals("active", stateCondition["value"])
    }

    @Test
    fun `should properly escape quotes in JSON`() {
        val filter = edLinkFilter {
            field("name") {
                equalsOperator("test\"value")
            }
        }

        // Parse as JSON to verify escaping works
        val parsed = objectMapper.readValue(filter, Map::class.java)

        val nameConditions = parsed["name"] as List<*>
        val nameCondition = nameConditions[0] as Map<*, *>
        assertEquals("test\"value", nameCondition["value"])
    }

    @Test
    fun `should properly escape backslashes in JSON`() {
        val filter = edLinkFilter {
            field("path") {
                equalsOperator("test\\value")
            }
        }

        // Parse as JSON to verify escaping works
        val parsed = objectMapper.readValue(filter, Map::class.java)

        val pathConditions = parsed["path"] as List<*>
        val pathCondition = pathConditions[0] as Map<*, *>
        assertEquals("test\\value", pathCondition["value"])
    }

    @Test
    fun `should handle in operator with string values`() {
        val filter = edLinkFilter {
            field("status") {
                inOperator("active,pending,completed")
            }
        }

        // Parse as JSON to verify it's valid
        val parsed = objectMapper.readValue(filter, Map::class.java)

        val statusConditions = parsed["status"] as List<*>
        val statusCondition = statusConditions[0] as Map<*, *>
        assertEquals("in", statusCondition["operator"])
        assertEquals("active,pending,completed", statusCondition["value"])
    }

    @Test
    fun `should handle empty filter builder`() {
        val filter = edLinkFilter {
            // No fields
        }

        // Parse as JSON to verify it's valid (empty object)
        val parsed = objectMapper.readValue(filter, Map::class.java)
        assertEquals(0, parsed.size)
        assertEquals("{}", filter)
    }

    @Test
    fun `should handle multiple conditions for same field`() {
        val filter = edLinkFilter {
            field("score") {
                equalsOperator("100")
                equalsOperator("200")
            }
        }

        // Parse as JSON to verify it's valid
        val parsed = objectMapper.readValue(filter, Map::class.java)

        val scoreConditions = parsed["score"] as List<*>
        assertEquals(2, scoreConditions.size)

        val condition1 = scoreConditions[0] as Map<*, *>
        assertEquals("eq", condition1["operator"])
        assertEquals("100", condition1["value"])

        val condition2 = scoreConditions[1] as Map<*, *>
        assertEquals("eq", condition2["operator"])
        assertEquals("200", condition2["value"])
    }

    @Test
    fun `should handle field names with special characters`() {
        val filter = edLinkFilter {
            field("user_id") {
                equalsOperator("123")
            }
            field("email-address") {
                equalsOperator("test@example.com")
            }
        }

        // Parse as JSON to verify it's valid
        val parsed = objectMapper.readValue(filter, Map::class.java)
        assertEquals(2, parsed.size)

        val userIdConditions = parsed["user_id"] as List<*>
        assertEquals(1, userIdConditions.size)

        val emailConditions = parsed["email-address"] as List<*>
        assertEquals(1, emailConditions.size)
    }
}
