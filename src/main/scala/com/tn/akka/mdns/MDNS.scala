package com.tn.akka.mdns

import akka.actor._
import javax.jmdns._
import com.typesafe.scalalogging.slf4j.Logging

/**
 * The companion object for `MDNS`.
 */
object MDNS {

  def apply(system: ActorSystem): ActorRef = system.actorOf(Props(classOf[MDNS]))
}

/**
 * A specialization of `Actor` that handles the MDNS control messages.
 */
private[mdns] class MDNS extends Actor with Logging {

  val jmdns = JmDNS.create()

  /**
   * @inheritdoc
   */
  override def receive: Actor.Receive = {

    case register: Register => {

      logger.debug(
        "Registering service: {} : {} on port {}",
        register.serviceType,
        register.serviceName,
        register.port.underlying()
      )

      jmdns.registerService(
        ServiceInfo.create(
          register.serviceType,
          register.serviceName,
          register.port,
          register.serviceDescription
        )
      )

      register.handler ! Registered(register.serviceType, register.serviceName, register.port)

      logger.debug(
        "Registered service: {} : {} on port {}",
        register.serviceType,
        register.serviceName,
        register.port.underlying()
      )
    }

    case unregister: Unregister => {

      logger.debug(
        "Unregistering service: {} : {} on port {}",
        unregister.serviceType,
        unregister.serviceName,
        unregister.port.underlying()
      )

      jmdns.unregisterService(
        ServiceInfo.create(
          unregister.serviceType,
          unregister.serviceName,
          unregister.port,
          unregister.serviceDescription
        )
      )

      unregister.handler ! Unregistered(unregister.serviceType, unregister.serviceName, unregister.port)

      logger.debug(
        "Unregistered service: {} : {} on port {}",
        unregister.serviceType,
        unregister.serviceName,
        unregister.port.underlying()
      )
    }

    case resolve: Resolve => {

      logger.debug("Resolving service type: {}", resolve.serviceType)

      jmdns.addServiceListener(resolve.serviceType, new ResolveServiceListener(resolve.handler))
      jmdns.list(resolve.serviceType, resolve.timeout.toMillis)
    }
  }

  /**
   * @inheritdoc
   */
  override def postStop(): Unit = {

    jmdns.unregisterAllServices()
    jmdns.close()
    super.postStop()
  }

  /**
   * An implementation of `ServiceListener` that is used in conjunction with `Resolve` requests.
   */
  private class ResolveServiceListener(handler: ActorRef) extends ServiceListener {

    /**
     * @inheritdoc
     */
    override def serviceAdded(serviceEvent: ServiceEvent) = {

      logger.debug("Service added: {} : {}", serviceEvent.getType, serviceEvent.getName)
      jmdns.requestServiceInfo(serviceEvent.getType, serviceEvent.getName)
    }

    /**
     * @inheritdoc
     */
    override def serviceRemoved(serviceEvent: ServiceEvent) = {

      logger.debug("Service added: {} : {}", serviceEvent.getType, serviceEvent.getName)

      handler ! Lost(
        serviceEvent.getType,
        serviceEvent.getName,
        serviceEvent.getInfo.getInetAddress,
        serviceEvent.getInfo.getPort
      )
    }

    /**
     * @inheritdoc
     */
    override def serviceResolved(serviceEvent: ServiceEvent) = {

      logger.debug("Service resolved: {} : {}", serviceEvent.getType, serviceEvent.getName)

      handler ! Resolved(
        serviceEvent.getType,
        serviceEvent.getName,
        serviceEvent.getInfo.getInetAddress,
        serviceEvent.getInfo.getPort
      )
    }
  }
}


