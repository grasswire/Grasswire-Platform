package com.grasswire.common.parsers

/**
 * Created by levinotik on 6/22/15.
 */
sealed trait LinkType {
  def linkType: String
}

object LinkTypes {
  val tweet = "tweet"
  val plainLink = "plain_link"
  val video = "video"
  val youtubeVideo = "video"
}

sealed trait VideoLinkType extends LinkType

case class TweetLinkType(tweetId: String) extends LinkType {
  override def linkType: String = LinkTypes.tweet
}

case object PlainLinkType extends LinkType {
  override def linkType: String = LinkTypes.plainLink
}

case class VineVideoLinkType(videoId: String) extends VideoLinkType {
  override def linkType: String = LinkTypes.video
}

case class YoutubeVideoLinkType(videoId: String) extends VideoLinkType {
  override def linkType: String = LinkTypes.video
}
