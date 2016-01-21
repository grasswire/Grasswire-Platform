package com.grasswire.common.json_models

import com.grasswire.common._
import com.grasswire.common.models.Tweet
import play.api.libs.json._

/**
 * Created by levinotik on 6/23/15.
 */
sealed trait LinkJsonModel {
  def submittedBy: Username
  def storyId: Long
  def createdAt: Long
  def hidden: Boolean
  def id: Long
  def user: UserJsonModel
}

case class VideoLinkJsonModel(linkData: VideoLinkJsonData,
  submittedBy: Username,
  storyId: Long,
  createdAt: Long,
  hidden: Boolean,
  id: Long,
  user: UserJsonModel) extends LinkJsonModel

case class TweetLinkJsonModel(tweet: Tweet,
  submittedBy: Username,
  storyId: Long,
  createdAt: Long,
  hidden: Boolean,
  id: Long,
  user: UserJsonModel) extends LinkJsonModel

case class PlainLinkJsonModel(linkData: PlainLinkJsonData,
  submittedBy: Username,
  storyId: Long,
  createdAt: Long,
  hidden: Boolean,
  id: Long,
  user: UserJsonModel) extends LinkJsonModel

//case class PhotoLinkJsonModel(linkData: PhotoLinkJsonData,
//  submittedBy: Username,
//  storyId: Long,
//  createdAt: Long,
//  hidden: Boolean,
//  id: Long,
//  user: UserJsonModel) extends LinkJsonModel

object LinkJsonModel {
  import julienrf.variants.Variants
  implicit val format: Format[LinkJsonModel] = Variants.format[LinkJsonModel]("type")
}
