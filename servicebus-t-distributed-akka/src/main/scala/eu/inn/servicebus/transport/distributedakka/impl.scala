package eu.inn.servicebus.transport.distributedakka

import java.io.{ByteArrayOutputStream, ByteArrayInputStream}

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.{SubscribeAck, Subscribe}
import eu.inn.servicebus.serialization._
import eu.inn.servicebus.transport.{Util, SubscriptionHandlerResult, Topic}

import scala.concurrent.Future
import scala.util.control.NonFatal
import akka.pattern.pipe

private [transport] trait Command

private [transport] case class Subscription[OUT, IN](topic: Topic,
                                                     groupName: Option[String],
                                                     inputDecoder: Decoder[IN],
                                                     partitionArgsExtractor: PartitionArgsExtractor[IN],
                                                     exceptionEncoder: Encoder[Throwable],
                                                     handler: (IN) => SubscriptionHandlerResult[OUT])

private [transport] case class Start[OUT,IN](id: String, subscription: Subscription[OUT,IN]) extends Command

private [transport] case object StopServer extends Command

private [transport] abstract class ServerActor[OUT,IN] extends Actor with ActorLogging {
  protected [this] val mediator = DistributedPubSubExtension(context.system).mediator
  protected [this] var subscription: Subscription[OUT,IN] = null

  override def receive: Receive = {
    case start: Start[OUT,IN] ⇒
      subscription = start.subscription
      mediator ! Subscribe(subscription.topic.url, Util.getUniqGroupName(subscription.groupName), self) // todo: test empty group behavior

    case ack: SubscribeAck ⇒
      context become start
  }

  def start: Receive

  protected def handleException(e: Throwable, sendReply: Boolean): Option[String] = {
    val msg = try {
      val outputBytes = new ByteArrayOutputStream()
      subscription.exceptionEncoder(e, outputBytes)
      Some(outputBytes.toString(Util.defaultEncoding))
    } catch {
      case NonFatal(e2) ⇒
        log.error(e2, "Can't encode exception: " + e)
        None
    }

    if (sendReply) {
      msg.foreach { s ⇒
        import context._
        Future.successful(s) pipeTo context.sender
      }
    }

    msg
  }

  protected def decodeMessage(input: String, sendReply: Boolean) = {

    try {
      val inputBytes = new ByteArrayInputStream(input.getBytes(Util.defaultEncoding))
      Some(subscription.inputDecoder(inputBytes))
    }
    catch {
      case NonFatal(e) ⇒
        handleException(e, sendReply)
        None
    }
  }
}

private [transport] class OnServerActor[OUT,IN] extends ServerActor[OUT,IN] {
  import context._

  def start: Receive = {
    case input: String ⇒
      decodeMessage(input, sendReply = true) map { inputMessage ⇒
        val result = subscription.handler(inputMessage)
        val futureMessage = result.futureResult.map { out ⇒
          val outputBytes = new ByteArrayOutputStream()
          result.resultEncoder(out, outputBytes)
          outputBytes.toString(Util.defaultEncoding)
        } recover {
          case NonFatal(e) ⇒ handleException(e, sendReply = false).getOrElse(throw e) // todo: test this scenario
        }
        futureMessage pipeTo sender
      }
  }
}

private [transport] class SubscribeServerActor[IN] extends ServerActor[Unit,IN] {
  import context._
  def start: Receive = {
    case input: String ⇒
      decodeMessage(input, sendReply = false) map { inputMessage ⇒
        subscription.handler(inputMessage).futureResult.recover {
          case NonFatal(e) ⇒ log.error(e, "Subscriber handler failed")
        }
      }
  }
}