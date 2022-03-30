package com.anahoret.imagilabsapi.teachingmaterials.storage

import org.springframework.data.repository.CrudRepository
import java.util.*

interface BundleLessonEntityRepository : CrudRepository<BundleLessonEntity, UUID>
