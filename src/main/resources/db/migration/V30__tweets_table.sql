CREATE TABLE tweets
(
    tweet_order BIGINT NOT NULL,
    tweet_uri   TEXT,

    CONSTRAINT pk_tweets PRIMARY KEY (tweet_order)
);

INSERT INTO tweets(tweet_order, tweet_uri)
VALUES (1, ''),
       (2, ''),
       (3, '');
