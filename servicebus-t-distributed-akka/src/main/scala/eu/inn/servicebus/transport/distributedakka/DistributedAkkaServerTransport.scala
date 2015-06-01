package eu.inn.servicebus.transport.distributedakka

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.concurrent.atomic.AtomicLong

import akka.actor._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import eu.inn.servicebus.serialization.{Decoder, PartitionArgsExtractor}
import eu.inn.servicebus.transport.{ServerTransport, SubscriptionHandlerResult, Topic}

import scala.collection.concurrent.TrieMap

class DistributedAkkaServerTransport(val actorSystem: ActorSystem = Util.akkaSystem) extends ServerTransport {
  val subscriptions = new TrieMap[String, ActorRef]
  protected val idCounter = new AtomicLong(0)

  override def on[OUT, IN](topic: Topic,
                           inputDecoder: Decoder[IN],
                           partitionArgsExtractor: PartitionArgsExtractor[IN])
                          (handler: (IN) ⇒ SubscriptionHandlerResult[OUT]): String = {

    val actor = actorSystem.actorOf(Props[OnServerActor[OUT,IN]])
    val id = idCounter.incrementAndGet().toHexString
    subscriptions.put(id, actor)
    actor ! Start(id, Subscription[OUT, IN](topic, None, inputDecoder, partitionArgsExtractor, handler))
    id
  }

  override def subscribe[IN](topic: Topic,
                             groupName: String,
                             inputDecoder: Decoder[IN],
                             partitionArgsExtractor: PartitionArgsExtractor[IN])
                            (handler: (IN) ⇒ SubscriptionHandlerResult[Unit]): String = {
    val actor = actorSystem.actorOf(Props[SubscribeServerActor[IN]])
    val id = idCounter.incrementAndGet().toHexString
    subscriptions.put(id, actor)
    actor ! Start(id, Subscription[Unit, IN](topic, Some(groupName), inputDecoder, partitionArgsExtractor, handler))
    id
  }

  override def off(subscriptionId: String): Unit = {
    subscriptions.get(subscriptionId).foreach{ s⇒
      actorSystem.stop(s)
      subscriptions.remove(subscriptionId)
    }
  }
}

private [distributedakka] trait Command

private [distributedakka] case class Subscription[OUT, IN](topic: Topic,
                                                           groupName: Option[String],
                                                           inputDecoder: Decoder[IN],
                                                           partitionArgsExtractor: PartitionArgsExtractor[IN],
                                                           handler: (IN) => SubscriptionHandlerResult[OUT])

private [distributedakka] case class Start[OUT,IN](id: String, subscription: Subscription[OUT,IN]) extends Command

private [distributedakka] case object StopServer extends Command

private [distributedakka] abstract class ServerActor[OUT,IN] extends Actor with ActorLogging {
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
}

private [distributedakka] class OnServerActor[OUT,IN] extends ServerActor[OUT,IN] {
  import context._
  import akka.pattern.pipe

  def start: Receive = {
    case input: String ⇒
      val inputBytes = new ByteArrayInputStream(input.getBytes(Util.defaultEncoding))
      val inputMessage = subscription.inputDecoder(inputBytes)
      val result = subscription.handler(inputMessage)
      val futureMessage = result.futureResult.map { out ⇒
        val outputBytes = new ByteArrayOutputStream()
        result.resultEncoder(out, outputBytes)
        outputBytes.toString(Util.defaultEncoding)
      }
      futureMessage pipeTo sender
  }
}

private [distributedakka] class SubscribeServerActor[IN] extends ServerActor[Unit,IN] {
  def start: Receive = {
    case input: String ⇒
      val inputBytes = new ByteArrayInputStream(input.getBytes(Util.defaultEncoding))
      val inputMessage = subscription.inputDecoder(inputBytes)
      subscription.handler(inputMessage)
  }
}