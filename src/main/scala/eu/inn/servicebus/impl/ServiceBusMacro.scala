package eu.inn.servicebus.impl

import scala.concurrent.Future
import scala.reflect.macros.blackbox.Context

private[servicebus] object ServiceBusMacro {
  def send[OUT: c.WeakTypeTag, IN: c.WeakTypeTag](c: Context)(topic: c.Expr[String],
                                                              message: c.Expr[IN]): c.Expr[Future[OUT]] = {
    import c.universe._

    val thiz = c.prefix.tree

    val out = weakTypeOf[OUT]
    val in = weakTypeOf[IN]

    val obj = q"""{
      import eu.inn.servicebus.serialization._
      val decoder = JsonDecoder.createDecoder[$out]
      val encoder = JsonEncoder.createEncoder[$in]
      val thiz = $thiz
      thiz.send[$out,$in]($topic,$message,decoder,encoder)
    }"""
    //println(obj)
    c.Expr[Future[OUT]](obj)
  }

  def subscribe[OUT: c.WeakTypeTag, IN: c.WeakTypeTag]
    (c: Context) (
      topic: c.Expr[String],
      groupName: c.Expr[Option[String]],
      handler: c.Expr[(IN) => Future[OUT]]
      ): c.Expr[String] = {

    import c.universe._

    val thiz = c.prefix.tree

    val out = weakTypeOf[OUT]
    val in = weakTypeOf[IN]

    val obj = q"""{
      import eu.inn.servicebus.serialization._
      val decoder = JsonDecoder.createDecoder[$out]
      val encoder = JsonEncoder.createEncoder[$in]
      val thiz = $thiz
      val id = thiz.subscribe[$out,$in]($topic,$groupName,decoder,encoder,$handler)
      id
    }"""
    //println(obj)
    c.Expr[String](obj)
  }
}