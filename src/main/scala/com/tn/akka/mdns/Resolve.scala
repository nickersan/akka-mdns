package com.tn.akka.mdns

import akka.actor.ActorRef
import scala.concurrent.duration.Duration

/**
 * Defines the message used to resolve a service with MDNS.
 */
case class Resolve(handler: ActorRef, serviceType: String, timeout: Duration = Duration.Undefined)
