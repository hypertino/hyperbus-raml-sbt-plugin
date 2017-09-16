import com.hypertino.hyperbus.raml.{GeneratorOptions, InterfaceGenerator}
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch.{Diff, Operation}
import org.raml.v2.api.RamlModelBuilder
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.JavaConversions
import scala.io.Source

class InterfaceGeneratorSpec extends FreeSpec with Matchers {
  val referenceValue = s"""
    object BookTag {
      type StringEnum = String
      val NEW = "new"
      val BEST_SELLER = "best-seller"
      val CLASSICS = "classics"
      lazy val values = Seq(NEW,BEST_SELLER,CLASSICS)
      lazy val valuesSet = values.toSet
    }

    case class BookProperties(
        publishYear: Short,
        sold: Int,
        issn: String,
        tag: BookTag.StringEnum
      )

    @body("book")
    case class Book(
        bookId: String,
        authorId: String,
        bookName: String,
        authorName: String,
        bookProperties: BookProperties
      ) extends Body

    object Book extends BodyObjectApi[Book] {
    }

    @body("book-transaction")
    case class BookTransaction(
        transactionId: String
      ) extends Body

    object BookTransaction extends BodyObjectApi[BookTransaction]

    @body("book-created-transaction")
    case class BookCreatedTransaction(
        transactionId: String
      ) extends Body

    object BookCreatedTransaction extends BodyObjectApi[BookCreatedTransaction]

    @body("click")
    case class Click(
        clickUrl: String,
        extra: com.hypertino.binders.value.Value
      ) extends Body

    object Click extends BodyObjectApi[Click]

    @body("click-confirmation")
    case class ClickConfirmation(
        id: String,
        extra: com.hypertino.binders.value.Value = com.hypertino.binders.value.Null
      ) extends Body

    object ClickConfirmation extends BodyObjectApi[ClickConfirmation]

    @body("clicks-information")
    case class ClicksInformation(
        count: Long,
        lastRegistered: Option[java.util.Date] = None,
        firstInserted: Option[java.util.Date] = None
      ) extends Body

    object ClicksInformation extends BodyObjectApi[ClicksInformation]

    case class Author(
      name: String,
      books: Seq[Book]
    )


    // --------------------

    @request(Method.GET, "hb://test/authors/{author_id}/books/{book_id}")
    case class AuthorBookGet(
        authorId: String,
        bookId: String,
        body: EmptyBody
      ) extends Request[EmptyBody]
      with DefinedResponse[
        Ok[Book]
      ]

    trait AuthorBookGetMetaCompanion {
      def apply(
        authorId: String,
        bookId: String,
        body: EmptyBody = EmptyBody,
        headers: com.hypertino.hyperbus.model.Headers = com.hypertino.hyperbus.model.Headers.empty,
        query: com.hypertino.binders.value.Value = com.hypertino.binders.value.Null
      ): AuthorBookGet
    }

    object AuthorBookGet extends com.hypertino.hyperbus.model.RequestMetaCompanion[AuthorBookGet] with AuthorBookGetMetaCompanion {
      implicit val meta = this
      type ResponseType = Ok[Book]
    }

    @request(Method.PUT, "hb://test/authors/{author_id}/books/{book_id}")
    case class AuthorBookPut(
        authorId: String,
        bookId: String,
        body: Book
      ) extends Request[Book]
      with DefinedResponse[(
        Ok[DynamicBody],
        Created[DynamicBody]
      )]

    trait AuthorBookPutMetaCompanion {
      def apply(
        authorId: String,
        bookId: String,
        body: Book,
        headers: com.hypertino.hyperbus.model.Headers = com.hypertino.hyperbus.model.Headers.empty,
        query: com.hypertino.binders.value.Value = com.hypertino.binders.value.Null
      ): AuthorBookPut
    }

    object AuthorBookPut extends com.hypertino.hyperbus.model.RequestMetaCompanion[AuthorBookPut] with AuthorBookPutMetaCompanion {
      implicit val meta = this
      type ResponseType = Response[DynamicBody]
    }

    @request(Method.FEED_PUT, "hb://test/authors/{author_id}/books/{book_id}")
    case class AuthorBookFeedPut(
        authorId: String,
        bookId: String,
        body: Book
      ) extends Request[Book]

    @request(Method.GET, "hb://test/authors/{author_id}/books")
    case class AuthorBooksGet(
        authorId: String,
        body: EmptyBody
      ) extends Request[EmptyBody]
      with DefinedResponse[
        Ok[DynamicBody]
      ]

    trait AuthorBooksGetMetaCompanion {
      def apply(
        authorId: String,
        body: EmptyBody = EmptyBody,
        headers: com.hypertino.hyperbus.model.Headers = com.hypertino.hyperbus.model.Headers.empty,
        query: com.hypertino.binders.value.Value = com.hypertino.binders.value.Null
      ): AuthorBooksGet
    }

    object AuthorBooksGet extends com.hypertino.hyperbus.model.RequestMetaCompanion[AuthorBooksGet] with AuthorBooksGetMetaCompanion {
      implicit val meta = this
      type ResponseType = Ok[DynamicBody]
    }

    @request(Method.POST, "hb://test/authors/{author_id}/books")
    case class AuthorBooksPost(
        authorId: String,
        body: DynamicBody
      ) extends Request[DynamicBody]
      with DefinedResponse[(
        Ok[BookTransaction],
        Created[BookCreatedTransaction]
      )]

    trait AuthorBooksPostMetaCompanion {
      def apply(
        authorId: String,
        body: DynamicBody,
        headers: com.hypertino.hyperbus.model.Headers = com.hypertino.hyperbus.model.Headers.empty,
        query: com.hypertino.binders.value.Value = com.hypertino.binders.value.Null
      ): AuthorBooksPost
    }

    object AuthorBooksPost extends com.hypertino.hyperbus.model.RequestMetaCompanion[AuthorBooksPost] with AuthorBooksPostMetaCompanion {
      implicit val meta = this
      type ResponseType = ResponseBase
    }

    @request(Method.POST, "hb://test/clicks")
    case class ClicksPost(
        body: Click
      ) extends Request[Click]
      with DefinedResponse[(
        Created[ClickConfirmation],
        Ok[ClickConfirmation]
      )]

    trait ClicksPostMetaCompanion {
      def apply(
        body: Click,
        headers: com.hypertino.hyperbus.model.Headers = com.hypertino.hyperbus.model.Headers.empty,
        query: com.hypertino.binders.value.Value = com.hypertino.binders.value.Null
      ): ClicksPost
    }

    object ClicksPost extends com.hypertino.hyperbus.model.RequestMetaCompanion[ClicksPost] with ClicksPostMetaCompanion {
      implicit val meta = this
      type ResponseType = Response[ClickConfirmation]
    }

    @request(Method.GET, "hb://test/clicks")
    case class ClicksGet(
        body: EmptyBody
      ) extends Request[EmptyBody]
      with DefinedResponse[
        Ok[ClickCollection]
      ]

    trait ClicksGetMetaCompanion {
      def apply(
        body: EmptyBody = EmptyBody,
        headers: com.hypertino.hyperbus.model.Headers = com.hypertino.hyperbus.model.Headers.empty,
        query: com.hypertino.binders.value.Value = com.hypertino.binders.value.Null
      ): ClicksGet
    }

    object ClicksGet extends com.hypertino.hyperbus.model.RequestMetaCompanion[ClicksGet] with ClicksGetMetaCompanion {
      implicit val meta = this
      type ResponseType = Ok[ClickCollection]
    }

    @body("click-collection")
    case class ClickCollection(items: Seq[Click]) extends CollectionBody[Click]

    object ClickCollection extends BodyObjectApi[ClickCollection] {
    }

    @request(Method.GET, "hb://test/clicks/{click_url}")
    case class ClickGet(
        clickUrl: String,
        sortBy: String,
        filter: Option[String],
        body: EmptyBody
      ) extends Request[EmptyBody]

    trait ClickGetMetaCompanion {
      def apply(
        clickUrl: String,
        sortBy: String,
        filter: Option[String] = None,
        body: EmptyBody = EmptyBody,
        headers: com.hypertino.hyperbus.model.Headers = com.hypertino.hyperbus.model.Headers.empty,
        query: com.hypertino.binders.value.Value = com.hypertino.binders.value.Null
      ): ClickGet
    }

    object ClickGet extends com.hypertino.hyperbus.model.RequestMetaCompanion[ClickGet] with ClickGetMetaCompanion {
      implicit val meta = this
      type ResponseType = ResponseBase
    }
  """

  def normalize(s: String): String = {
    val step1 = s.foldLeft (("",true)) { case ((r: String, prevIsSpace: Boolean), c: Char) ⇒
      val c2 = c match {
        case '\r' ⇒ ' '
        case '\t' ⇒ ' '
        case _ ⇒ c
      }
      if (c2 == ' ' && prevIsSpace) {
        (r, prevIsSpace)
      }
      else {
        (r + c2, c2 == ' ' || c2 == '\n')
      }
    }._1
    step1.replace("  ", " ").trim()
  }

  "RAML" in {
    val result = generateCode("test.raml")

    result should include("package com.hypertino.raml")
    val idx = result.indexOf("\nobject BookTag {")
    if (idx < 0) {
      println("=======================================================================")
      println(result)
      println("=======================================================================")
      fail("RAML generator doesn't contain permanent marker")
    }
    val resultPermanent = result.substring(idx)

    val resultPermanentNormalized = normalize(resultPermanent)
    val referenceValueNormalized = normalize(referenceValue)

    if (resultPermanentNormalized.indexOf(referenceValueNormalized) < 0) {
      println("=======================================================================")
      println(result)
      println("=======================================================================")
      val diff = new DiffMatchPatch
      val diffResult = diff.diffMain(resultPermanentNormalized, referenceValueNormalized, false)
      import JavaConversions._
      diffResult.foreach {
        case d: Diff if d.operation == Operation.EQUAL ⇒
          print(d.text)
        case d: Diff if d.operation == Operation.INSERT ⇒
          print("\n+++>")
          print(d.text)
          print("<+++\n")
        case d: Diff if d.operation == Operation.DELETE ⇒
          print("\n--->")
          print(d.text)
          print("<---\n")
      }
      fail("RAML generator doesn't return reference text")
    }
  }

  def generateCode(path: String): String = {
    import JavaConversions._
    val resource = this.getClass.getResource(path)
    if (resource == null) {
      throw new IllegalArgumentException(s"resource not found: $path")
    }
    val source = Source.fromURL(resource).getLines().mkString("\n")

    val api = new RamlModelBuilder().buildApi(source,path)

    val validationErrors = api.getValidationResults.mkString("\n")
    val apiV10 = api.getApiV10
    if (apiV10 == null) {
      fail(validationErrors)
    }
    else {
      println(validationErrors)
    }

    val gen = new InterfaceGenerator(apiV10, GeneratorOptions(packageName = "com.hypertino.raml"))
    gen.generate()
  }
}
