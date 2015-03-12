package com.neo.sk.arachnez.proxy

import akka.actor.{ActorLogging, Actor, Props}
import com.neo.sk.arachnez.core.Launch
import com.neo.sk.arachnez.framework.{Tick, ProxyGet, Proxy}
import org.slf4j.{LoggerFactory, Logger}

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/10/15
 * Time: 15:07
 *
//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//
//   █████▒█    ██  ▄████▄   ██ ▄█▀       ██████╗ ██╗   ██╗ ██████╗
// ▓██   ▒ ██  ▓██▒▒██▀ ▀█   ██▄█▒        ██╔══██╗██║   ██║██╔════╝
// ▒████ ░▓██  ▒██░▒▓█    ▄ ▓███▄░        ██████╔╝██║   ██║██║  ███╗
// ░▓█▒  ░▓▓█  ░██░▒▓▓▄ ▄██▒▓██ █▄        ██╔══██╗██║   ██║██║   ██║
// ░▒█░   ▒▒█████▓ ▒ ▓███▀ ░▒██▒ █▄       ██████╔╝╚██████╔╝╚██████╔╝
//  ▒ ░   ░▒▓▒ ▒ ▒ ░ ░▒ ▒  ░▒ ▒▒ ▓▒       ╚═════╝  ╚═════╝  ╚═════╝
//  ░     ░░▒░ ░ ░   ░  ▒   ░ ░▒ ▒░
//  ░ ░    ░░░ ░ ░ ░        ░ ░░ ░
//           ░     ░ ░      ░  ░
// 
 *
 */


object ProxyUtils {
  lazy val proxyActor = Launch().actorOf(Props[ProxyPoolActor], "proxyPoolActor")

  def apply() = proxyActor
}

class ProxyPoolActor extends Actor{
  private val logger: Logger = LoggerFactory.getLogger(classOf[ProxyPoolActor])
  private val usableProxy = new mutable.Queue[String]
  private val waitingProxy = new mutable.Queue[String]
  private val allProxy = new mutable.HashSet[String]

  context.system.scheduler.schedule(0 milliseconds, 1 minutes, self, Tick)

  override def receive: Receive = {
    case Tick =>
      logger.info("start fetching proxy")
      if(usableProxy.size<100 && allProxy.size<200){
        logger.info("need fetch")
        val f = Future{
          import scala.collection.JavaConversions.asScalaSet
          val proxys = ProxyGetUtil.getProxy
          asScalaSet(proxys)
        }
        f.onComplete{
          case Success(proxys) =>
            proxys.map{
              case proxy:String if allProxy.add(proxy) =>
                waitingProxy.enqueue(proxy)
            }
          case Failure(e) =>
        }
      }
    case ProxyGet =>
      if (usableProxy.isEmpty) {
        sender() ! Proxy(None)
      } else {
        val proxy = usableProxy.dequeue()
        sender() ! Proxy(Some(proxy))
        usableProxy.enqueue(proxy)
      }
  }
}

