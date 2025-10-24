package com.anahoret.imagilabsapi.edlink.storage

import com.anahoret.imagilabsapi.common.storage.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "ed_link_oauth_state")
class EdLinkOAuthStateEntity : BaseEntity()
