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
