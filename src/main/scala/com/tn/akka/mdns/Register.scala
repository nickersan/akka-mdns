package com.tn.akka.mdns

import akka.actor.ActorRef

/**
 * Defines the message used to register a service with MDNS.
 */
case class Register(handler: ActorRef, serviceType: String, serviceName: String, port: Int, serviceDescription: String)
