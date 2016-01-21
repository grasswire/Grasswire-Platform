package com.grasswire.users

import scalaz.Monad

trait UserManager[M[+_]] {
  def M: Monad[M]
}

