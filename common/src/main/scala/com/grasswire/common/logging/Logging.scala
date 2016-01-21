package com.grasswire.common.logging

import org.slf4j.LoggerFactory

trait Logging {
  val logger = Logger(this.getClass)
}

object Logger {
  import org.slf4j.{ Logger => Slf4jLogger }
  def apply(clazz: Class[_]): Slf4jLogger = LoggerFactory.getLogger(clazz.getSimpleName)
}
