package com.hypertino.hyperbus.raml

case class RamlSource(path: String, packageName: String, isResource: Boolean = false, contentTypePrefix: Option[String] = None)
