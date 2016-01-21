// package com.grasswire.common.rmq
// 
// import com.grasswire.common.RMQMessageHeaders
// import com.rabbitmq.client.QueueingConsumer.Delivery
// import scala.collection.JavaConverters._
// 
// case class GWMessage(routingKey: String, body: Array[Byte], deliveryTag: Long, headers: RMQMessageHeaders)
// 
// object GWMessage {
// 
//   def apply(delivery: Delivery) = toGWMessage(delivery)
// 
//   def toGWMessage(delivery: Delivery): GWMessage = {
//     var h = Map.empty[String, String]
//     delivery.getProperties.getHeaders.asScala.foreach { case (k: String, v: Any) => h += (k -> v.toString) }
//     GWMessage(delivery.getEnvelope.getRoutingKey, delivery.getBody, delivery.getEnvelope.getDeliveryTag,
//       h)
//   }
// 
//   implicit class GWDeliveryOps(delivery: Delivery) {
//     def asMessage = GWMessage.toGWMessage(delivery)
//   }
// 
// }
