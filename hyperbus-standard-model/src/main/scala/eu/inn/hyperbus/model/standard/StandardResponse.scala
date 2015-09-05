package eu.inn.hyperbus.model.standard

import eu.inn.hyperbus.model.{Body, Response}
import eu.inn.hyperbus.serialization.ResponseHeader

object StandardResponse {
  def apply(responseHeader: ResponseHeader, body: Body): Response[Body] = {
    val messageId = responseHeader.messageId
    val correlationId = responseHeader.correlationId.getOrElse(messageId)
    responseHeader.status match {
      case Status.OK => Ok(body, messageId, correlationId)
      case Status.CREATED => Created(body.asInstanceOf[CreatedBody], messageId, correlationId)
      case Status.ACCEPTED => Accepted(body, messageId, correlationId)
      case Status.NON_AUTHORITATIVE_INFORMATION => NonAuthoritativeInformation(body, messageId, correlationId)
      case Status.NO_CONTENT => NoContent(body, messageId, correlationId)
      case Status.RESET_CONTENT => ResetContent(body, messageId, correlationId)
      case Status.PARTIAL_CONTENT => PartialContent(body, messageId, correlationId)
      case Status.MULTI_STATUS => MultiStatus(body, messageId, correlationId)

      case Status.MULTIPLE_CHOICES => MultipleChoices(body, messageId, correlationId)
      case Status.MOVED_PERMANENTLY => MovedPermanently(body, messageId, correlationId)
      case Status.FOUND => Found(body, messageId, correlationId)
      case Status.SEE_OTHER => SeeOther(body, messageId, correlationId)
      case Status.NOT_MODIFIED => NotModified(body, messageId, correlationId)
      case Status.USE_PROXY => UseProxy(body, messageId, correlationId)
      case Status.TEMPORARY_REDIRECT => TemporaryRedirect(body, messageId, correlationId)

      case Status.BAD_REQUEST => BadRequest(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.UNAUTHORIZED => Unauthorized(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.PAYMENT_REQUIRED => PaymentRequired(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.FORBIDDEN => Forbidden(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.NOT_FOUND => NotFound(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.METHOD_NOT_ALLOWED => MethodNotAllowed(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.NOT_ACCEPTABLE => NotAcceptable(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.PROXY_AUTHENTICATION_REQUIRED => ProxyAuthenticationRequired(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.REQUEST_TIMEOUT => RequestTimeout(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.CONFLICT => Conflict(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.GONE => Gone(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.LENGTH_REQUIRED => LengthRequired(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.PRECONDITION_FAILED => PreconditionFailed(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.REQUEST_ENTITY_TOO_LARGE => RequestEntityTooLarge(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.REQUEST_URI_TOO_LONG => RequestUriTooLong(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.UNSUPPORTED_MEDIA_TYPE => UnsupportedMediaType(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.REQUESTED_RANGE_NOT_SATISFIABLE => RequestedRangeNotSatisfiable(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.EXPECTATION_FAILED => ExpectationFailed(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.UNPROCESSABLE_ENTITY => UnprocessableEntity(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.LOCKED => Locked(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.FAILED_DEPENDENCY => FailedDependency(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.TOO_MANY_REQUEST => TooManyRequest(body.asInstanceOf[ErrorBody], null, correlationId)

      case Status.INTERNAL_SERVER_ERROR => InternalServerError(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.NOT_IMPLEMENTED => NotImplemented(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.BAD_GATEWAY => BadGateway(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.SERVICE_UNAVAILABLE => ServiceUnavailable(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.GATEWAY_TIMEOUT => GatewayTimeout(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.HTTP_VERSION_NOT_SUPPORTED => HttpVersionNotSupported(body.asInstanceOf[ErrorBody], null, correlationId)
      case Status.INSUFFICIENT_STORAGE => InsufficientStorage(body.asInstanceOf[ErrorBody], null, correlationId)
    }
  }
}
