package com.tn.akka.mdns

/**
 * Defines the message used to indicate a service has been registered with MDNS.
 */
case class Unregistered(serviceType: String, serviceName: String, port: Int)

