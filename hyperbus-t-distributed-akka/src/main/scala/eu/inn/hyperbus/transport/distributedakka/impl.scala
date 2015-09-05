package eu.inn.hyperbus.transport.distributedakka

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import akka.actor.Actor
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.{Subscribe, SubscribeAck, Unsubscribe}
import akka.pattern.pipe
import eu.inn.hyperbus.transport.api._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.control.NonFatal

private[transport] trait Command

private[transport] case class Subscription[OUT, IN <: TransportRequest](topicUrl: String,
                                                                        topic: Topic,
                                                                        groupName: Option[String],
                                                                        inputDeserializer: Deserializer[IN],
                                                                        exceptionSerializer: Serializer[Throwable],
                                                                        handler: (IN) => Future[OUT])

private[transport] case class Start[OUT, IN <: TransportRequest](id: String, subscription: Subscription[OUT, IN], logMessages: Boolean) extends Command

private[transport] abstract class ServerActor[OUT, IN <: TransportRequest] extends Actor {
  protected[this] val mediator = DistributedPubSubExtension(context.system).mediator
  protected[this] var subscription: Subscription[OUT, IN] = null
  protected[this] var logMessages = false
  protected[this] var log = LoggerFactory.getLogger(getClass)

  override def receive: Receive = {
    case start: Start[OUT, IN] ⇒
      subscription = start.subscription
      logMessages = start.logMessages
      mediator ! Subscribe(subscription.topicUrl, Util.getUniqGroupName(subscription.groupName), self) // todo: test empty group behavior

    case ack: SubscribeAck ⇒
      context become start
  }

  override def postStop() {
    mediator ! Unsubscribe(subscription.topicUrl, self)
  }

  def start: Receive

  protected def handleException(e: Throwable, sendReply: Boolean): Option[String] = {
    val msg = try {
      val outputBytes = new ByteArrayOutputStream()
      subscription.exceptionSerializer(e, outputBytes)
      Some(outputBytes.toString(Util.defaultEncoding))
    } catch {
      case NonFatal(e2) ⇒
        log.error("Can't serialize exception: " + e, e2)
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
      Some(subscription.inputDeserializer(inputBytes))
    }
    catch {
      case NonFatal(e) ⇒
        handleException(e, sendReply)
        None
    }
  }
}

private[transport] class ProcessServerActor[IN <: TransportRequest] extends ServerActor[TransportResponse, IN] {

  import context._
  import eu.inn.hyperbus.util.LogUtils._

  def start: Receive = {
    case input: String ⇒
      if (logMessages && log.isTraceEnabled) {
        log.trace(Map("requestId" → input.hashCode.toHexString,
          "subscriptionId" → subscription.handler.hashCode.toHexString), s"hyperBus ~> $input")
      }

      decodeMessage(input, sendReply = true) map { inputMessage ⇒
        val result = subscription.handler(inputMessage) // todo: test result with partitonArgs?
      val futureMessage = result.map { out ⇒
          val outputBytes = new ByteArrayOutputStream()
          out.serialize(outputBytes)
          outputBytes.toString(Util.defaultEncoding)
        } recover {
          case NonFatal(e) ⇒ handleException(e, sendReply = false).getOrElse(throw e) // todo: test this scenario
        }
        if (logMessages && log.isTraceEnabled) {
          futureMessage map { s ⇒
            log.trace(Map("requestId" → input.hashCode.toHexString,
              "subscriptionId" → subscription.handler.hashCode.toHexString), s"hyperBus <~(R)~ $s")
            s
          } pipeTo sender
        }
        else {
          futureMessage pipeTo sender
        }
      }
  }
}

private[transport] class SubscribeServerActor[IN <: TransportRequest] extends ServerActor[Unit, IN] {

  import context._
  import eu.inn.hyperbus.util.LogUtils._

  def start: Receive = {
    case input: String ⇒
      if (logMessages && log.isTraceEnabled) {
        log.trace(Map("subscriptionId" → subscription.handler.hashCode.toHexString), s"hyperBus |> $input")
      }
      decodeMessage(input, sendReply = false) map { inputMessage ⇒
        subscription.handler(inputMessage).recover {
          // todo: test result with partitonArgs?
          case NonFatal(e) ⇒ log.error(Map("subscriptionId" → subscription.handler.hashCode.toHexString),
            "Subscriber handler failed", e)
        }
      }
  }
}

/*
private [transport] class NoRouteWatcher extends Actor with ActorLogging {
  import context._
  system.eventStream.subscribe(self, classOf[DeadLetter])

  override def receive: Receive = {
    case deadMessage: DeadLetter ⇒
      Future.failed(new NoTransportRouteException(deadMessage.recipient.toString())) pipeTo deadMessage.sender
  }
}
*/