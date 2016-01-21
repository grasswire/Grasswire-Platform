package com.grasswire.common.models

case class TweetModel(tweetIdStr: String,
  data: String,
  submittedBy: Option[String],
  feedId: Long)
