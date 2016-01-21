package com.grasswire.common.config

import com.grasswire.common.db.DAL

case class GWEnvironment(redis: scredis.Redis, dal: DAL) {
  def db = dal.db
}
