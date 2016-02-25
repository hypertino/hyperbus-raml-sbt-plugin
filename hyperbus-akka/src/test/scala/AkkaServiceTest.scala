
import akka.actor.{Actor, ActorSystem}
import akka.testkit.TestActorRef
import akka.util.Timeout
import eu.inn.binders.annotations.fieldName
import eu.inn.binders.dynamic.{Null, Text}
import eu.inn.hyperbus.transport.api.matchers.{TransportRequestMatcher, AnyValue}
import eu.inn.hyperbus.transport.api.uri.Uri
import eu.inn.hyperbus.{HyperBus, IdGenerator}
import eu.inn.hyperbus.akkaservice.annotations.group
import eu.inn.hyperbus.akkaservice.{AkkaHyperService, _}
import eu.inn.hyperbus.model._
import eu.inn.hyperbus.model.annotations.{body, request}
import eu.inn.hyperbus.transport._
import eu.inn.hyperbus.transport.api._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

@body("application/vnd+test-1.json")
case class AkkaTestBody1(resourceData: String) extends Body

@body("application/vnd+test-2.json")
case class AkkaTestBody2(resourceData: Long) extends Body

@body("application/vnd+created-body.json")
case class AkkaTestCreatedBody(resourceId: String,
                           @fieldName("_links") links: LinksMap.LinksMapType = LinksMap("/resources/{resourceId}"))
  extends CreatedBody

// with NoContentType

@body("application/vnd+test-error-body.json")
case class AkkaTestErrorBody(code: String,
                         description: Option[String] = None,
                         errorId: String = IdGenerator.create()) extends ErrorBody {
  def message = code + description.map(": " + _).getOrElse("")

  def extra = Null
}


@request(Method.POST, "/resources")
case class AkkaTestPost1(body: AkkaTestBody1) extends Request[AkkaTestBody1]
with DefinedResponse[Created[AkkaTestCreatedBody]]

@request(Method.POST, "/resources")
case class AkkaTestPost3(body: AkkaTestBody2) extends Request[AkkaTestBody2]
with DefinedResponse[(Ok[DynamicBody], Created[AkkaTestCreatedBody], NotFound[AkkaTestErrorBody])]

class TestActor extends Actor {
  var count = 0

  def receive = AkkaHyperService.dispatch(this)

  def ~>(implicit testPost1: AkkaTestPost1) = {
    count += 1
    Future {
      Created(AkkaTestCreatedBody("100500"))
    }
  }

  def ~> (implicit testPost3: AkkaTestPost3) = {
    count += 1
    Future {
      if (testPost3.body.resourceData == 1)
        Created(AkkaTestCreatedBody("100500"))
      else
      if (testPost3.body.resourceData == -1)
        throw Conflict(ErrorBody("failed"))
      else
      if (testPost3.body.resourceData == -2)
        Conflict(ErrorBody("failed"))
      else
      if (testPost3.body.resourceData == -3)
        NotFound(AkkaTestErrorBody("not_found"))
      else
        Ok(DynamicBody(Text("another result")))
    }
  }
}

class TestGroupActor extends Actor {
  var count = 0

  def receive = AkkaHyperService.dispatch(this)

  @group("group1")
  def |>(testPost1: AkkaTestPost1) = {
    count += 1
    Future.successful {}
  }
}

class AkkaHyperServiceTest extends FreeSpec with ScalaFutures with Matchers {

  def newHyperBus = {
    val tr = new InprocTransport
    val cr = List(TransportRoute[ClientTransport](tr, TransportRequestMatcher(Some(Uri(AnyValue)))))
    val sr = List(TransportRoute[ServerTransport](tr, TransportRequestMatcher(Some(Uri(AnyValue)))))
    val transportManager = new TransportManager(cr, sr, ExecutionContext.global)
    new HyperBus(transportManager, logMessages = true)
  }

  "AkkaHyperService " - {
    "Send and Receive" in {
      implicit lazy val system = ActorSystem()
      val hyperBus = newHyperBus
      val actorRef = TestActorRef[TestActor]
      val groupActorRef = TestActorRef[TestGroupActor]

      implicit val timeout = Timeout(20.seconds)
      hyperBus.routeTo[TestActor](actorRef).futureValue.map(println)
      hyperBus.routeTo[TestGroupActor](groupActorRef).futureValue.map(println)

      val f1 = hyperBus <~ AkkaTestPost1(AkkaTestBody1("ha ha"))

      whenReady(f1) { r =>
        //r.messageId should equal("123")
        //r.correlationId should equal("xyz")
        r.body should equal(AkkaTestCreatedBody("100500"))
        actorRef.underlyingActor.count should equal(1)
        groupActorRef.underlyingActor.count should equal(1)
      }

      val f2 = hyperBus <| AkkaTestPost1(AkkaTestBody1("ha ha"))

      whenReady(f2) { r =>
        actorRef.underlyingActor.count should equal(2)
        groupActorRef.underlyingActor.count should equal(2)
      }
      Await.result(system.terminate(), 10.second)
    }

    "Send and Receive multiple responses" in {
      implicit lazy val system = ActorSystem()
      val hyperBus = newHyperBus
      val actorRef = TestActorRef[TestActor]
      implicit val timeout = Timeout(20.seconds)
      hyperBus.routeTo[TestActor](actorRef)

      val f = hyperBus <~ AkkaTestPost3(AkkaTestBody2(1))

      whenReady(f) { r =>
        r shouldBe a[Created[_]]
        r.body should equal(AkkaTestCreatedBody("100500"))
      }

      val f2 = hyperBus <~ AkkaTestPost3(AkkaTestBody2(2))

      whenReady(f2) { r =>
        r shouldBe a[Ok[_]]
        r.body should equal(DynamicBody(Text("another result")))
      }

      val f3 = hyperBus <~ AkkaTestPost3(AkkaTestBody2(-1))

      whenReady(f3.failed) { r =>
        r shouldBe a[Conflict[_]]
      }

      val f4 = hyperBus <~ AkkaTestPost3(AkkaTestBody2(-2))

      whenReady(f4.failed) { r =>
        r shouldBe a[Conflict[_]]
      }

      val f5 = hyperBus <~ AkkaTestPost3(AkkaTestBody2(-3))

      whenReady(f5.failed) { r =>
        r shouldBe a[NotFound[_]]
        r.asInstanceOf[Response[_]].body shouldBe a[AkkaTestErrorBody]
      }
      Await.result(system.terminate(), 10.second)
    }
  }
}
