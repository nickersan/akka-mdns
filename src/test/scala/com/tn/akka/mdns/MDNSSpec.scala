package com.tn.akka.mdns

import java.net.InetAddress

import scala.concurrent.duration._

import akka.actor.{Props, Actor, ActorRef, ActorSystem}
import akka.testkit.{TestProbe, ImplicitSender, TestKit}

import org.junit.runner.RunWith

import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers

/**
 * Test spec for `MDNS`.
 */
@RunWith(classOf[JUnitRunner])
class MDNSSpec extends TestKit(ActorSystem.create())
  with ImplicitSender
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll {

  "MDNS" should {

    val serviceName = "testService"
    val serviceType = "_test._tcp.local."
    val port = 5001

    "fire a message when a service is registered" in {

      val messageProbe = TestProbe()
      system.actorOf(Props(classOf[Server], serviceName, serviceType, port, messageProbe.ref))

      val registered = messageProbe.fishForMessage(30 seconds) {
        case Registered(_, _, _) => true
        case _ => false
      }

      assert(registered.isInstanceOf[Registered])
      assert(registered.asInstanceOf[Registered].serviceName === serviceName)
      assert(registered.asInstanceOf[Registered].serviceType === serviceType)
      assert(registered.asInstanceOf[Registered].port === port)
    }

    "allow a client to find a given service" in {

      val clientMessageProbe = TestProbe()
      system.actorOf(Props(classOf[Client], serviceType, clientMessageProbe.ref))

      val resolved = clientMessageProbe.fishForMessage(5 seconds) {
        case Resolved(_, _, _, _) => true
        case _ => false
      }

      assert(resolved.isInstanceOf[Resolved])
      assert(resolved.asInstanceOf[Resolved].serviceName === serviceName)
      assert(resolved.asInstanceOf[Resolved].address === InetAddress.getLocalHost)
    }
  }
}

/**
 * A example server that shows how `MDNS` should be combined with an ''actor''.
 */
class Server(serviceName: String, serviceType: String, port: Int, messageProbe: ActorRef) extends Actor {

  MDNS(context.system) ! Register(messageProbe, serviceType, serviceName, port, "Testing")

  /**
   * @inheritdoc
   */
  def receive: Actor.Receive = {

    case message @ _ => messageProbe ! message
  }
}

/**
 * A example client that shows how `MDNS` should be combined with an ''actor''.
 */
class Client(serviceType: String, messageProbe: ActorRef) extends Actor {

  MDNS(context.system) ! Resolve(messageProbe, serviceType, 10 seconds)

  /**
   * @inheritdoc
   */
  def receive: Actor.Receive = {

    case message @ _ => messageProbe ! message
  }
}

