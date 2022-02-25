package com.anahoret.imagilabsapi.projects.storage;

import org.springframework.data.repository.CrudRepository
import java.util.*

interface ProjectEntityRepository : CrudRepository<ProjectEntity, UUID>
