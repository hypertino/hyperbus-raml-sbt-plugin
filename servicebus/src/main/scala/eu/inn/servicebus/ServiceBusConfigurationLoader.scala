package eu.inn.servicebus

import com.typesafe.config.{ConfigFactory, ConfigObject, ConfigValue, Config}
import eu.inn.servicebus.transport._
import eu.inn.servicebus.util.ConfigUtils

class ServiceBusConfigurationError(message: String) extends RuntimeException(message)

object ServiceBusConfigurationLoader {
  import scala.collection.JavaConversions._
  import ConfigUtils._

  def fromConfig(config: Config): ServiceBusConfiguration = {
    val sc = config.getConfig("service-bus")

    val st = sc.getObject("transports")
    val transportMap = st.entrySet().map { entry ⇒
      val transportTag = entry.getKey
      val transportConfig = sc.getConfig("transports."+transportTag)
      val transport = createTransport(transportConfig)
      transportTag → transport
    }.toMap

    ServiceBusConfiguration(
      sc.getConfigList("client-routes").map{ li⇒
        getTransportRoute[ClientTransport](transportMap, li)
      }.toSeq,
      sc.getConfigList("server-routes").map{ li⇒
        getTransportRoute[ServerTransport](transportMap, li)
      }.toSeq
    )
  }

  private def getTransportRoute[T](transportMap: Map[String, Any], config: Config): TransportRoute[T] = {
    val transportName = config.getString("transport")
    val transport = transportMap.getOrElse(transportName,
      throw new ServiceBusConfigurationError(s"Couldn't find transport '$transportName' at ${config.origin()}")
    ).asInstanceOf[T]

    val urlArg = getPartitionArg(config.getOptionString("url").getOrElse(""), config.getOptionString("match-type"))

    val partitionArgs = config.getOptionObject("partition-args").map { c⇒
      readPartitionArgs(c, config)
    } getOrElse {
      PartitionArgs(Map())
    }

    TransportRoute[T](transport, urlArg, partitionArgs)
  }

  private def createTransport(config: Config): Any = {
    val className = {
      val s = config.getString("class-name")
      if (s.contains("."))
        s
      else
        "eu.inn.servicebus.transport." + s
    }
    val clazz = Class.forName(className)
    val transportConfig = config.getOptionConfig("configuration").getOrElse(
      ConfigFactory.parseString("")
    )
    clazz.getConstructor(classOf[Config]).newInstance(transportConfig)
  }

  private def getPartitionArg(value: String, matchType: Option[String]) = matchType match {
    case Some("Any") ⇒ AnyArg
    case Some("Exact") ⇒ ExactArg(value)
    case Some("Regex") ⇒ RegexArg(value)
    case _ ⇒ ExactArg(value)
  }

  private def readPartitionArgs(config: ConfigObject, parent: Config) = PartitionArgs(
    config.toMap.keys.map { key ⇒
      val c = parent.getConfig(s"partition-args.$key")
      val value = c.getOptionString("value").getOrElse("")
      val matchType = c.getOptionString("match-type")
      (key, getPartitionArg(value, matchType))
    }.toMap
  )
}