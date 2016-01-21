package com.grasswire.common.parsers

import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by levinotik on 6/23/15.
 */
class LinkTypeParsersSpec extends FlatSpec with Matchers {

  "A LinkTypeParser" should "parse a vine video url as a VineVideoLinkType" in {
    LinkTypeParsers.linktype("https://vine.co/v/eeYm3ddglhn") shouldBe VineVideoLinkType("eeYm3ddglhn")
  }

  it should "parse a tweet url as a TweetLinkType" in {
    LinkTypeParsers.linktype("https://twitter.com/docker/status/613382676515438592") shouldBe TweetLinkType("613382676515438592")
  }

  it should "parse a youtube video url as a YoutubeLinkType" in {
    LinkTypeParsers.linktype("https://www.youtube.com/watch?v=KJGM2-YHnns") shouldBe YoutubeVideoLinkType("KJGM2-YHnns")
    LinkTypeParsers.linktype("https://youtu.be/KJGM2-YHnns") shouldBe Some(YoutubeVideoLinkType("KJGM2-YHnns"))
  }
}
