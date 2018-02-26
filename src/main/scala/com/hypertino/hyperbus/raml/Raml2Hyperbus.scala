/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.hypertino.hyperbus.raml

import java.io.{File, FileNotFoundException}

import org.raml.v2.api.RamlModelBuilder
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

import scala.collection.JavaConverters._

object Raml2Hyperbus extends AutoPlugin {
  override def requires = JvmPlugin
  override def trigger = allRequirements
  object autoImport {
    val ramlHyperbusSources = settingKey[Seq[RamlSource]]("ramlHyperbusSources")
    def ramlSource(
                    path: String,
                    packageName: String,
                    isResource: Boolean = false,
                    contentTypePrefix: Option[String] = None,
                    baseClasses: Map[String, Seq[String]] = Map.empty
                  ): RamlSource = RamlSource(path, packageName, isResource, contentTypePrefix, baseClasses)
  }

  import autoImport._

  override val projectSettings =
    ramlHyperbusScopedSettings(Compile) //++ ramlHyperbusScopedSettings(Test) ++*/ //r2hDefaultSettings

  protected def ramlHyperbusScopedSettings(conf: Configuration): Seq[Def.Setting[_]] = inConfig(conf)(
    Seq(
      sourceGenerators in conf +=  Def.task {
        ramlHyperbusSources.value.map { source ⇒
          generateFromRaml(resourceDirectory.value, new File(source.path), source.isResource, sourceManaged.value,
            source.packageName, source.contentTypePrefix, source.baseClasses)
        }
      }.taskValue
    )
  )

  protected def generateFromRaml(resourceDirectory: File,
                                 source: File,
                                 sourceIsResource: Boolean,
                                 base: File, packageName: String,
                                 contentPrefix: Option[String],
                                 baseClasses: Map[String, Seq[String]]): File = {

    val outputFile = base / "r2h" / (packageName.split('.').mkString("/") + "/" + source.getName + ".scala")
    val apiFile = if (sourceIsResource) {
      resourceDirectory / source.getPath
    } else {
      source
    }
    if (!outputFile.canRead || outputFile.lastModified() < apiFile.lastModified()) {
      if (!apiFile.exists()) {
        throw new FileNotFoundException(s"File ${apiFile.getAbsolutePath} doesn't exists")
      }

      val api = new RamlModelBuilder().buildApi(apiFile)
      val ramlApi = api.getApiV10
      if (ramlApi == null) {
        val validationErrors = api.getValidationResults.asScala.mkString(System.lineSeparator())
        throw new RuntimeException(s"RAML parser errors for '${apiFile.getAbsolutePath}':${System.lineSeparator()} $validationErrors")
      }
      val generator = new InterfaceGenerator(ramlApi, GeneratorOptions(packageName, baseClasses, contentPrefix))
      IO.write(outputFile, generator.generate())
    }
    outputFile
  }
}
