package com.grasswire.common

import java.util.Properties

import com.typesafe.config.ConfigFactory

object CommonConfig {

  val superUsers = List("AustenAllred", "LeviNotik", "grasswire", "joecianflone")

  val grasswireConfig = ConfigFactory.load().getConfig("grasswire-config")

  val slackHook = grasswireConfig.getString("slack-hook")

  val environment = grasswireConfig.getString("env")

  def setGlobalLogger() =
    if (CommonConfig.isStage) {
      System.setProperty("logback.configurationFile", "logback.logentries.stage.xml")
    } else if (CommonConfig.isProd) {
      System.setProperty("logback.configurationFile", "logback.logentries.prod.xml")
    } else {
      System.setProperty("logback.configurationFile", "logback.local.xml")
    }

  def isProd = environment == "prod"

  def isStage = environment == "stage"

  object DatabaseConfig {
    private val databaseConfig = grasswireConfig.getConfig("database-config")
    val jdbcUrl = databaseConfig.getString("url")
    val user = databaseConfig.getString("properties.user")
    val password = databaseConfig.getString("properties.password")
  }

  def mailchimpList = if (isProd) "2d4482fd83" else "94bafd42d1"

  val mailchimpAPIKey = grasswireConfig.getString("mailchimp-api-key")

  object Sendgrid {
    val config = grasswireConfig.getConfig("sendgrid")
    val username = config.getString("username")
    val password = config.getString("password")
  }

  object TwitterConfig {
    object Submisions {
      val accessKey =
        grasswireConfig.getString("twitter-submissions.access-key")
      val accessSecret =
        grasswireConfig.getString("twitter-submissions.access-secret")
      val consumerKey =
        grasswireConfig.getString("twitter-submissions.consumer-key")
      val consumerSecret =
        grasswireConfig.getString("twitter-submissions.consumer-secret")
    }

    object Auth {
      val config = grasswireConfig.getConfig("twitter-auth")
      val consumerKey = config.getString("consumer-key")
      val consumerSecret = config.getString("consumer-secret")
    }

  }
}
