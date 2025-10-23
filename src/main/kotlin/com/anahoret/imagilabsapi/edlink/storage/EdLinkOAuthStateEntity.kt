package com.anahoret.imagilabsapi.edlink.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "ed_link_oauth_state")
class EdLinkOAuthStateEntity(id: UUID) : BaseEntity(id = id)
