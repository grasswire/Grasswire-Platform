package com.grasswire.common.parsers

import com.grasswire.common.regex.Regexes

/**
 * Created by levinotik on 6/22/15.
 */

object LinkTypeParsers {

  def linktype: String => LinkType = url =>
    Regexes.twitterStatusId(url).map(TweetLinkType) orElse Regexes.youtubeVideoId(url).map(YoutubeVideoLinkType) orElse Regexes.vineVideoId(url).map(VineVideoLinkType) getOrElse PlainLinkType
}
