package com.grasswire.api.http.routing

import com.grasswire.common.models.StoryEdits
import com.grasswire.common.{ Username, CommonConfig }
import com.grasswire.common.json_models._
import net.gpedro.integrations.slack.{ SlackMessage, SlackApi }

sealed trait SlackNotification
case class NotifyStoryCreated(storyJsonModel: StoryJsonModel) extends SlackNotification
case class NotifyLinkSubmitted(linkJsonModel: LinkJsonModel) extends SlackNotification
case class NotifyStoryHidden(id: Long) extends SlackNotification
case class NotifyStoryEdited(storyEdits: StoryEdits) extends SlackNotification

object SlackNotifications {

  private[this] def mkStoryLink(id: Long) = if (CommonConfig.isStage) s"https://beta.grasswire.com/story/$id/x" else s"https://grasswire.com/story/$id/x"

  def notify(slackNotification: SlackNotification, username: Username)(slackApi: SlackApi): Unit = if (CommonConfig.environment == "local") () else slackNotification match {
    case NotifyStoryCreated(story) => slackApi.call(new SlackMessage("#site-updates", s"Grasswire API - ${CommonConfig.environment}",
      s"""$username created a new story
         | name: ${story.name}
         | headline: ${story.headline.getOrElse("")}
         | summary: ${story.summary.getOrElse("")}
         | cover photo: ${story.coverPhoto.getOrElse("")}""".stripMargin))

    case NotifyLinkSubmitted(link) =>
      val message: SlackMessage = link match {
        case v: VideoLinkJsonModel => new SlackMessage("#site-updates", s"Grasswire API - ${CommonConfig.environment}",
          s"""$username submitted a video
              | url: ${v.linkData.url}
              | thumbnail: ${v.linkData.thumbnail.getOrElse("")}
              | title: ${v.linkData.title}
              | description: ${v.linkData.description}""".stripMargin)

        case t: TweetLinkJsonModel => new SlackMessage("#site-updates", s"Grasswire API - ${CommonConfig.environment}", s"$username submitted a tweet ${t.tweet.tweetUrl}")

        case p: PlainLinkJsonModel => new SlackMessage("#site-updates", s"Grasswire API - ${CommonConfig.environment}",
          s"""$username submitted a link
              | url: ${p.linkData.url}
              | thumbnail: ${p.linkData.thumbnail.getOrElse("")}
              | title: ${p.linkData.title}
              | description: ${p.linkData.description}""".stripMargin)

      }
      slackApi.call(message)

    case NotifyStoryHidden(id) => slackApi.call(new SlackMessage("#site-updates", s"Grasswire API - ${CommonConfig.environment}",
      s"""$username hid a story
         |url: ${mkStoryLink(id)}
       """.stripMargin))

    case NotifyStoryEdited(edits) if edits.edits.nonEmpty => slackApi.call(new SlackMessage("#site-updates", s"Grasswire API - ${CommonConfig.environment}", username + " " + edits.edits.mkString("\n")))

    case _ => {}
  }

}
