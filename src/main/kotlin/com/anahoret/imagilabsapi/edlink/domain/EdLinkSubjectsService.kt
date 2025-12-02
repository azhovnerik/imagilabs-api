package com.anahoret.imagilabsapi.edlink.domain

import arrow.core.Either
import arrow.core.right
import com.anahoret.imagilabsapi.edlink.api.EdLinkClassApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkEnrollmentApi
import com.anahoret.imagilabsapi.edlink.api.EdLinkSubjectApi
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service to retrieve teacher's subjects from EdLink API
 * Uses enrollments -> classes -> subjects chain to get only subjects where person is a teacher
 */
@Service
class EdLinkSubjectsService(
    private val edLinkEnrollmentApi: EdLinkEnrollmentApi,
    private val edLinkClassApi: EdLinkClassApi,
    private val edLinkSubjectApi: EdLinkSubjectApi
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Retrieves teacher's subjects as a comma-separated string
     * Returns null if:
     * - No active teacher enrollments found
     * - No classes with subject_id found
     * - Any API call fails (graceful degradation)
     *
     * @param token Integration access token for EdLink API
     * @param personId Person UUID to get subjects for
     * @return Either<Nothing, String?> where null means no subjects or error occurred
     */
    fun getTeacherSubjects(token: String, personId: UUID): Either<Nothing, String?> {
        logger.debug("Fetching subjects for teacher personId: $personId")

        // Step 1: Get active teacher enrollments
        val enrollments = edLinkEnrollmentApi.getTeacherEnrollments(token, personId)
            .fold(
                { error ->
                    logger.warn("Failed to fetch enrollments for person $personId: $error")
                    return null.right()
                },
                { it }
            )

        if (enrollments.isEmpty()) {
            logger.debug("No active teacher enrollments found for person $personId")
            return null.right()
        }

        logger.debug("Found ${enrollments.size} active teacher enrollments for person $personId")

        // Step 2: Extract class IDs from enrollments
        val classIds = enrollments.map { it.classId }.distinct()

        if (classIds.isEmpty()) {
            logger.debug("No class IDs found in enrollments for person $personId")
            return null.right()
        }

        // Step 3: Get classes by IDs
        val classes = edLinkClassApi.getClasses(token, classIds)
            .fold(
                { error ->
                    logger.warn("Failed to fetch classes for person $personId: $error")
                    return null.right()
                },
                { it }
            )

        if (classes.isEmpty()) {
            logger.debug("No classes found for person $personId")
            return null.right()
        }

        logger.debug("Found ${classes.size} classes for person $personId")

        // Step 4: Extract subject IDs from classes (ignoring null)
        val subjectIds = classes.mapNotNull { it.subjectId }.distinct()

        if (subjectIds.isEmpty()) {
            logger.debug("No subject IDs found in classes for person $personId")
            return null.right()
        }

        logger.debug("Found ${subjectIds.size} unique subject IDs for person $personId")

        // Step 5: Get subjects by IDs
        val subjects = edLinkSubjectApi.getSubjects(token, subjectIds)
            .fold(
                { error ->
                    logger.warn("Failed to fetch subjects for person $personId: $error")
                    return null.right()
                },
                { it }
            )

        if (subjects.isEmpty()) {
            logger.debug("No subjects found for person $personId")
            return null.right()
        }

        // Step 6: Extract names, deduplicate, sort, and join
        val subjectNames = subjects
            .map { it.name }
            .distinct()
            .sorted()
            .joinToString(", ")

        logger.debug("Successfully retrieved subjects for person $personId: $subjectNames")

        return subjectNames.right()
    }
}