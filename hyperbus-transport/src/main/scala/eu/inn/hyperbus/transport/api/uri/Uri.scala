package eu.inn.hyperbus.transport.api.uri

import com.typesafe.config.ConfigValue
import eu.inn.binders.core.{BindOptions, ImplicitDeserializer, ImplicitSerializer}
import eu.inn.binders.json.{JsonDeserializer, JsonSerializer}
import eu.inn.binders.naming.PlainConverter

import scala.language.postfixOps

case class Uri(pattern: UriPart, args: Map[String, UriPart]) {
  def matchUri(other: Uri): Boolean = pattern.matchUriPart(other.pattern) &&
    UriParts.matchUriParts(args, other.args)

  override def toString = s"Uri($pattern$argsFormat)"

  def matchArgs(other: Map[String, UriPart]) = UriParts.matchUriParts(args, other)

  private [this] def argsFormat =
    if (args.isEmpty) ""
  else
    args.mkString("#", ",", "")

  private [api] def toPojoJson = {
    val m = args.map {
      case (key, value) ⇒ key → value.specific
    }
    UriPojoJson(pattern.specific, if (m.isEmpty) None else Some(m))
  }

  lazy val formatted: String = UriParser.tokens(pattern.specific) collect {
    case TextToken(s) ⇒ s
    case SlashToken ⇒ '/'
    case ParameterToken(parameterName, _) ⇒ args(parameterName).specific
  } mkString
}

object Uri {
  def apply(pattern: UriPart): Uri = Uri(pattern, Map.empty[String,UriPart])

  def apply(pattern: String): Uri = Uri(SpecificValue(pattern))

  def apply(pattern: String, args: Map[String, String]): Uri = Uri(SpecificValue(pattern), args.map {
    case (k,v) ⇒ k → SpecificValue(v)
  })

  def apply(configValue: ConfigValue): Uri = {
    import eu.inn.binders.tconfig._
    val pojo = configValue.read[UriPojo]
    apply(pojo)
  }

  private [api] def apply(pojo: UriPojo): Uri = {
    Uri(
      UriPart(pojo.pattern),
      pojo.args.map { args ⇒
        args.map { case (k, v) ⇒
          k → UriPart(v)
        }
      } getOrElse {
        Map.empty[String, UriPart]
      }
    )
  }

  private [api] def apply(pojo: UriPojoJson): Uri = {
    apply(pojo.pattern, pojo.args.getOrElse(Map.empty).map(kv ⇒ kv._1 → kv._2))
  }
}

private[api] case class UriPojo(pattern: UriPartPojo, args: Option[Map[String, UriPartPojo]])
private[api] case class UriPojoJson(pattern: String, args: Option[Map[String, String]])

// todo: use generic type instead PlainConverter
class UriJsonDeserializer(implicit val bindOptions: BindOptions) extends ImplicitDeserializer[Uri, JsonDeserializer[PlainConverter]] {
  override def read(deserializer: JsonDeserializer[PlainConverter]): Uri = Uri(deserializer.unbind[UriPojoJson])
}

class UriJsonSerializer(implicit val bindOptions: BindOptions) extends ImplicitSerializer[Uri, JsonSerializer[PlainConverter]] {
  override def write(serializer: JsonSerializer[PlainConverter], value: Uri) = serializer.bind(value.toPojoJson)
}