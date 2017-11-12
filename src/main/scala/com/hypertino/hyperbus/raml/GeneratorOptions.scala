/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.hypertino.hyperbus.raml

case class GeneratorOptions(packageName: String,
                            contentTypePrefix: Option[String] = None,
                            generatorInformation: Boolean = true,
                            defaultImports:Boolean = true,
                            customImports: Option[String] = None,
                            dateType: String = "java.util.Date",
                            ramlTypeNameStyle: NameStyle.Value = NameStyle.DASH,
                            ramlFieldsNameStyle: NameStyle.Value = NameStyle.SNAKE,
                            ramlEnumNameStyle: NameStyle.Value = NameStyle.DASH,
                            classNameStyle: NameStyle.Value = NameStyle.PASCAL,
                            classFieldsStype: NameStyle.Value = NameStyle.CAMEL,
                            enumFieldsStyle: NameStyle.Value = NameStyle.SNAKE_UPPER
                           )
