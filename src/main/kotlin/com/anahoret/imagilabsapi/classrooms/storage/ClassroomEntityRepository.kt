package com.anahoret.imagilabsapi.classrooms.storage;

import org.springframework.data.repository.CrudRepository
import java.util.*

interface ClassroomEntityRepository : CrudRepository<ClassroomEntity, UUID>
