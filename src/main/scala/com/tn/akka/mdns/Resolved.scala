package com.tn.akka.mdns

import java.net.InetAddress

/**
 * Defines the message used to indicate a service has been resolved.
 */
case class Resolved(serviceType: String, serviceName: String, address: InetAddress, port: Int)
