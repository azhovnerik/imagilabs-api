package com.anahoret.imagilabsapi.tweets.storage

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TweetEntityRepository : JpaRepository<TweetEntity, UUID>
