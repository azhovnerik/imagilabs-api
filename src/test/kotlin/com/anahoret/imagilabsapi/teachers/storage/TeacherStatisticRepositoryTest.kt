package com.anahoret.imagilabsapi.teachers.storage

import com.anahoret.imagilabsapi.classrooms.domain.TeacherRole
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntity
import com.anahoret.imagilabsapi.classrooms.storage.ClassroomEntityRepository
import com.anahoret.imagilabsapi.common.testClassroom
import com.anahoret.imagilabsapi.common.testStudent
import com.anahoret.imagilabsapi.common.testTeacher
import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherEntity
import com.anahoret.imagilabsapi.coteachers.storage.CoTeacherRepository
import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntity
import com.anahoret.imagilabsapi.projectclassroomshare.storage.ProjectClassroomShareEntityRepository
import com.anahoret.imagilabsapi.projects.storage.ProjectEntity
import com.anahoret.imagilabsapi.projects.storage.ProjectEntityRepository
import com.anahoret.imagilabsapi.spring.ImagiLabsDatabaseTest
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntity
import com.anahoret.imagilabsapi.students.storage.StudentProfileEntityRepository
import com.anahoret.imagilabsapi.users.UserType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

@ImagiLabsDatabaseTest
@DisplayName("Teacher statistic repository")
class TeacherStatisticRepositoryTest {

    @Autowired
    lateinit var teacherStatisticRepository: TeacherStatisticRepository

    @Autowired
    lateinit var teacherProfileRepository: TeacherProfileEntityRepository

    @Autowired
    lateinit var classroomEntityRepository: ClassroomEntityRepository

    @Autowired
    lateinit var studentProfileRepository: StudentProfileEntityRepository

    @Autowired
    lateinit var projectRepository: ProjectEntityRepository

    @Autowired
    lateinit var projectSharedRepository: ProjectClassroomShareEntityRepository

    @Autowired
    lateinit var coTeacherRepository: CoTeacherRepository

    lateinit var teacherId: UUID
    lateinit var coTeacherId: UUID
    lateinit var classroomId: UUID
    lateinit var studentIds: List<UUID>
    lateinit var projectsIds: List<UUID>

    @BeforeEach
    fun setup() {
        setupTeacher()
        setupClassroom()
        setupCoTeacher()
        setupStudents()
        setupProjects()
        setupProjectShared()
    }

    fun setupTeacher() {
        val testTeacher = testTeacher()
        teacherId = teacherProfileRepository.save(
            TeacherProfileEntity(
                email = testTeacher.email,
                passwordHash = "",
                firstName = testTeacher.firstName,
                lastName = testTeacher.lastName,
                country = testTeacher.country,
                organization = testTeacher.organization,
                howDidYouHearAboutUs = "",
                howDidYouHearAboutUsOther = null,
                marketingEmailSubscribed = true,
                emailVerified = true,
                tipTokens = 3
            )
        ).id!!
    }

    fun setupClassroom() {
        val testClassroom = testClassroom(teacherId)
        classroomId = classroomEntityRepository.save(
            ClassroomEntity(
                name = testClassroom.name,
                accessCode = testClassroom.accessCode,
                teacherId = teacherId
            )
        ).id!!
    }

    fun setupCoTeacher() {
        val testTeacher = testTeacher()
        coTeacherId = teacherProfileRepository.save(
            TeacherProfileEntity(
                email = "co-teacher@gmail.com",
                passwordHash = "",
                firstName = testTeacher.firstName,
                lastName = testTeacher.lastName,
                country = testTeacher.country,
                organization = testTeacher.organization,
                howDidYouHearAboutUs = "",
                howDidYouHearAboutUsOther = null,
                marketingEmailSubscribed = true,
                emailVerified = true,
                tipTokens = 3
            )
        ).id!!

        coTeacherRepository.save(
            CoTeacherEntity(
                classroomId = classroomId,
                teacherEmail = "co-teacher@gmail.com",
                teacherId = coTeacherId,
                coTeacherStatus = TeacherRole.CO_TEACHER
            )
        )
    }

    fun setupStudents() {
        val testStudent = testStudent(classroomId)
        studentIds = studentProfileRepository.saveAll(
            listOf(
                StudentProfileEntity(
                    name = testStudent.name,
                    username = "student 1",
                    password = "",
                    classroomId = classroomId,
                    tipTokens = 1
                ),
                StudentProfileEntity(
                    name = testStudent.name,
                    username = "student 2",
                    password = "",
                    classroomId = classroomId,
                    tipTokens = 1
                )
            )
        ).map { it.id!! }
    }

    fun setupProjects() {
        projectsIds = projectRepository.saveAll(
            listOf(
                ProjectEntity("Project1", studentIds[0], UserType.STUDENT, "", ""),
                ProjectEntity("Project2", studentIds[0], UserType.STUDENT, "", ""),
                ProjectEntity("Project3", studentIds[1], UserType.STUDENT, "", ""),
                ProjectEntity("Project4", studentIds[1], UserType.STUDENT, "", ""),
                ProjectEntity("Project5", studentIds[1], UserType.STUDENT, "", ""),
                ProjectEntity("Project6", teacherId, UserType.TEACHER, "", "")
            )
        ).map { it.id!! }
    }

    fun setupProjectShared() {
        projectSharedRepository.saveAll(
            listOf(
                ProjectClassroomShareEntity(projectsIds[0], classroomId),
                ProjectClassroomShareEntity(projectsIds[3], classroomId)
            )
        )
    }

    @Test
    fun `fetch statistic data for teacher test`() {
        val teacherStatistic = teacherStatisticRepository.getTeacherStatistic(teacherId)!!
        with(teacherStatistic) {
            assertEquals(1, activeClassrooms)
            assertEquals(2, studentAccounts)
            assertEquals(2, studentSharedProjects)
            assertEquals(3, studentDraftProjects)
        }
    }

    @Test
    fun `fetch statistic data for co-teacher test`() {
        val teacherStatistic = teacherStatisticRepository.getTeacherStatistic(coTeacherId)!!
        with(teacherStatistic) {
            assertEquals(1, activeClassrooms)
            assertEquals(2, studentAccounts)
            assertEquals(2, studentSharedProjects)
            assertEquals(3, studentDraftProjects)
        }
    }
}
