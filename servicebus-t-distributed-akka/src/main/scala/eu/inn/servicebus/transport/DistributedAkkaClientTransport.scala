package eu.inn.servicebus.transport

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Props, ActorSystem}
import akka.cluster.Cluster
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Publish
import akka.util.Timeout
import com.typesafe.config.{ConfigFactory, Config}
import eu.inn.servicebus.serialization.{Decoder, Encoder}
import eu.inn.servicebus.transport.distributedakka.NoRouteWatcher
import eu.inn.servicebus.util.ConfigUtils

import scala.concurrent.duration.{FiniteDuration, Duration}
import scala.concurrent.{Promise, ExecutionContext, Future}
import ConfigUtils._

class DistributedAkkaClientTransport(val actorSystem: ActorSystem,
              val localAffinity: Boolean = true,
              implicit val executionContext: ExecutionContext = ExecutionContext.global,
              implicit val timeout: Timeout = Util.defaultTimeout) extends ClientTransport {

  def this(config: Config) = this(ActorSystemRegistry.getOrCreate(config.getString("actor-system", "eu-inn")),
    config.getOptionBoolean("local-afinity") getOrElse true,
    scala.concurrent.ExecutionContext.global, // todo: configurable ExecutionContext like in akka?
    new Timeout(config.getOptionDuration("timeout") getOrElse Util.defaultTimeout)
  )

  val noRouteActor = actorSystem.actorSelection("no-route-watcher").resolveOne().recover {
    case _ ⇒ actorSystem.actorOf(Props(new NoRouteWatcher), "no-route-watcher")
  }

  protected [this] val mediator = DistributedPubSubExtension(actorSystem).mediator


  override def ask[OUT, IN](topic: Topic,
                            message: IN,
                            inputEncoder: Encoder[IN],
                            outputDecoder: Decoder[OUT]): Future[OUT] = {

    val inputBytes = new ByteArrayOutputStream()
    inputEncoder(message, inputBytes)
    val messageString = inputBytes.toString(Util.defaultEncoding)

    akka.pattern.ask(mediator, Publish(topic.url, messageString, sendOneMessageToEachGroup = true)) map {
      case result: String ⇒
        val outputBytes = new ByteArrayInputStream(result.getBytes(Util.defaultEncoding))
        outputDecoder(outputBytes)
      // todo: case _ ⇒
    }
  }

  override def publish[IN](topic: Topic, message: IN, inputEncoder: Encoder[IN]): Future[Unit] = {
    val inputBytes = new ByteArrayOutputStream()
    inputEncoder(message, inputBytes)
    val messageString = inputBytes.toString(Util.defaultEncoding)
    mediator ! Publish(topic.url, messageString, sendOneMessageToEachGroup = true) // todo: At least one confirm?
    Future.successful{}
  }

  def shutdown(duration: FiniteDuration): Future[Boolean] = {
    val promise = Promise[Boolean]()
    if (!actorSystem.isTerminated) {
      actorSystem.registerOnTermination(promise.success(true))
      actorSystem.shutdown()
    }
    else {
      promise.success(true)
    }
    promise.future
  }
}

