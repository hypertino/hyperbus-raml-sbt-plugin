/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.hypertino.hyperbus.raml.utils

import com.hypertino.hyperbus.raml.NameStyle
import com.hypertino.inflector.naming._

class StyleConverter(from: NameStyle.Value, to: NameStyle.Value) extends BaseConverter{
  private val javaUnLegalChars = "[^A-Za-z0-9$_]".r

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
    val name = if (from == to) {
      identifier
    }
    else {
      super.convert(identifier)
    }

    javaUnLegalChars.findFirstIn(name).map(_ => s"`$name`") getOrElse name
  }
}
