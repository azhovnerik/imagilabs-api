package com.anahoret.imagilabsapi.studentclassroomlink.storage;

import org.springframework.data.repository.CrudRepository
import java.util.*

interface StudentClassroomLinkEntityRepository : CrudRepository<StudentClassroomLinkEntity, UUID>
