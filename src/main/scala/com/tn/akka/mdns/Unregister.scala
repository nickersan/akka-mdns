package com.tn.akka.mdns

import akka.actor.ActorRef

/**
 * Defines the message used to unregister a service with MDNS.
 */
case class Unregister(handler: ActorRef, serviceType: String, serviceName: String, port: Int, serviceDescription: String)
