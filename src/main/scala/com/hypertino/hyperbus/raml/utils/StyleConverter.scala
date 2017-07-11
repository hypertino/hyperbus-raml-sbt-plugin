package com.hypertino.hyperbus.raml.utils

import com.hypertino.hyperbus.raml.NameStyle
import com.hypertino.inflector.naming._

class StyleConverter(from: NameStyle.Value, to: NameStyle.Value) extends BaseConverter{
  import NameStyle._
  override protected def parser: IdentifierParser = {
    from match {
      case CAMEL ⇒ CamelCaseParser
      case PASCAL ⇒ CamelCaseParser
      case SNAKE ⇒ SnakeCaseParser
      case SNAKE_UPPER ⇒ SnakeCaseParser
      case DASH ⇒ DashCaseParser
    }
  }

  override protected def createBuilder(): IdentifierBuilder = to match {
    case CAMEL ⇒ new CamelCaseBuilder()
    case PASCAL ⇒ new PascalCaseBuilder()
    case SNAKE ⇒ new SnakeCaseBuilder()
    case SNAKE_UPPER ⇒ new SnakeUpperCaseBuilder()
    case DASH ⇒ new DashCaseBuilder()
  }

  override def convert(identifier : String) : String = {
    if (from == to) {
      identifier
    }
    else {
      super.convert(identifier)
    }
  }
}
