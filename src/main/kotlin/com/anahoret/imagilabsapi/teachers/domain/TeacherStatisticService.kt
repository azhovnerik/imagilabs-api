package com.anahoret.imagilabsapi.teachers.domain

import com.anahoret.imagilabsapi.teachers.storage.TeacherStatistic
import com.anahoret.imagilabsapi.teachers.storage.TeacherStatisticRepository
import org.springframework.stereotype.Service
import java.util.UUID

interface TeacherStatisticService {

    fun getTeacherStatistic(teacherId: UUID): TeacherStatistic?
}

@Service
class TeacherStatisticServiceImpl(
    private val teacherStatisticRepository: TeacherStatisticRepository
): TeacherStatisticService {

    override fun getTeacherStatistic(teacherId: UUID): TeacherStatistic? {
        return teacherStatisticRepository.getTeacherStatistic(teacherId)
    }
}
