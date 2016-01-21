// package com.grasswire.common.types
// 
// import com.grasswire.common.json_models.VoteJsonModel
// 
// trait CanSortByPopular[A] {
//   def votes(a: A): List[VoteJsonModel]
//   def createdAt(a: A): Long
// }
// 
// case class PopularSortable(votes: List[VoteJsonModel], createdAt: Long)
// 
// object PopularSortable {
//   def toPopularSortable[A](a: A)(implicit ev: CanSortByPopular[A]) = PopularSortable(ev.votes(a), ev.createdAt(a))
// }