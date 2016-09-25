package com.tn.akka.mdns

import java.net.InetAddress

/**
 * Defines the message used to indicate a service has been lost.
 */
case class Lost(serviceType: String, serviceName: String, address: InetAddress, port: Int)
