package com.anahoret.imagilabsapi.tweets.storage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "tweets")
class TweetEntity(

    @Id
    @Column(name = "tweet_order", nullable = false)
    var tweetOrder: Long,

    @Column(name = "tweet_uri")
    var tweetUri: String?
)
