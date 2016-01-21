package com.grasswire.api.config

import com.typesafe.config.ConfigFactory
import java.util.Properties
import com.rabbitmq.client.AMQP.BasicProperties

object APIConfig {
  private val config = ConfigFactory.load()

  val grasswireConfig = config.getConfig("grasswire-config")
  val environment = grasswireConfig.getString("env")

  def getSwaggerBaseUrl = environment match {
    case "stage" => "http://api-stage.grasswire.com/v1"
    case "prod" => "http://api-prod.grasswire.com/v1"
    case _ => "http://localhost:8080/v1"
  }

  object DatabaseConfig {
    private val databaseConfig = grasswireConfig.getConfig("database-config")
    val jdbcUrl = databaseConfig.getString("url")
    val user = databaseConfig.getString("properties.user")
    val password = databaseConfig.getString("properties.password")

    def getDbProperties = {
      val properties = new Properties()
      properties.setProperty("jdbcUrl", jdbcUrl)
      properties.setProperty("user", user)
      properties.setProperty("password", password)
      properties.setProperty("driverClass", "org.postgresql.Driver")
      properties
    }
  }

  object HttpConfig {
    private val httpConfig = config.getConfig("http")
    val interface = httpConfig.getString("interface")
    lazy val port = httpConfig.getInt("port")
  }
}
