package com.anahoret.imagilabsapi.teachers.domain

import org.springframework.stereotype.Service

interface TeacherAccessService {
    fun canDelete(userProfile: TeacherProfile, teacherProfile: TeacherProfile): Boolean
}

@Service
class TeacherAccessServiceImpl: TeacherAccessService {
    override fun canDelete(userProfile: TeacherProfile, teacherProfile: TeacherProfile): Boolean {
        return userProfile.id == teacherProfile.id
    }
}
