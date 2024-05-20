package com.anahoret.imagilabsapi.openai.storage

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface OpenAiAssistanceRepository : CrudRepository<OpenAiAssistanceEntity, UUID>
