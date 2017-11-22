/*
 * Copyright (c) 2017 Magomed Abdurakhmanov, Hypertino
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.hypertino.hyperbus.raml

case class RamlSource(path: String,
                      packageName: String,
                      isResource: Boolean = false,
                      contentTypePrefix: Option[String] = None,
                      baseClasses: Map[String, Seq[String]] = Map.empty
                     )
