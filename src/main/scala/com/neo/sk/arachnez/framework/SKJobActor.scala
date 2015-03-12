package com.neo.sk.arachnez.framework

import java.util
import java.util.concurrent.Executors

import akka.actor.Actor.Receive
import akka.actor._
import chen.bupt.httpclient.utils.{ResponseUtils, InputStreamUtils}
import com.neo.sk.arachnez.client.SKClientJob
import com.neo.sk.arachnez.commons.{SKJobInfoActor, SKURL, SKProperties}
import com.neo.sk.arachnez.proxy.ProxyPool
import com.neo.sk.arachnez.util.{URLUtils, HttpClientUtil}
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.util.EntityUtils
import org.slf4j.{LoggerFactory, Logger}
import scala.collection.mutable
import scala.concurrent._
import scala.concurrent.duration._

import scala.collection.mutable.Queue
import scala.util.{Failure, Success}

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/10/11
 * Time: 15:18
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
class SKJobActor(properties: SKProperties, seeds: List[String]) extends Actor {
  private val logger: Logger = LoggerFactory.getLogger(classOf[SKJobActor])
  implicit val Exe = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(properties.getThreadNum + 20))
  private var state = false
  private val urlActor = context.actorOf(Props[UrlPoolActor](new UrlPoolActor), "urlactor")
  private val extractActor = context.actorOf(Props[ExtractActor](new ExtractActor), "extractactor")
  private val jobInfoActor = context.actorOf(Props[SKJobInfoActor](new SKJobInfoActor(properties.getJobName)), "jobinfoactor")
  //jm add
  private val clientJob: SKClientJob = properties.getSKCJob
  private val userClient: CloseableHttpClient = HttpClientUtil.getHttpClient(properties.getSKCJob.getCookieStore, properties.getThreadNum)
  private var deadCount = 0

  override def receive: Receive = {
    case KillJob =>
      jobInfoActor ! Stop
      context.stop(self)
    //      context become gracefulKill // TODO: should? or just kill
    case StartJob =>
      state match {
        case running if state =>
          logger.info("ERROR: already running")
        case quite =>
          logger.info("start running now")
          state = true
          URLUtils.clearJobURL(properties.getJobName)
          deadCount = 0
          jobInfoActor ! InitJobInfo //jm add
          jobStart
      }
    case url: SKURL =>
      urlActor ! url
    case Terminated(actor) =>
      logger.info(actor.path.name + " is dead")
      deadCount += 1
      if (deadCount == properties.getThreadNum) {
        state = false
        logger.info(properties.getJobName + "is over")
        if (properties.getDelaySeconds == 0) {
          jobInfoActor ! Stop
          context.stop(self)
        } else {
          logger.info("will sleep " + properties.getDelaySeconds + " seconds")
          jobInfoActor ! Sleep //jm add
          context.system.scheduler.scheduleOnce(properties.getDelaySeconds seconds, self, StartJob)
        }
        clientJob.flush(System.getProperty("user.dir") + "/statistic/"
          + properties.getJobName + "/") // jm add
      }
    case _ =>
      logger.warn("unknown message from " + sender().path.name)
  }

  private def jobStart = {
    logger.info("start job " + properties.getJobName)
    jobInfoActor ! Run //jm add
    clientJob.initContext() //jm add
    (0 until seeds.size).map {
      case index =>
        urlActor ! new SKURL(seeds(index), true)
    }
    (0 until properties.getThreadNum).map {
      case index =>
        val fetchChild = context.actorOf(Props[FetchActor](new FetchActor), "child-" + index)
        context watch fetchChild
        fetchChild ! Next
    }
  }

  private def gracefulKill: Receive = {
    case msg =>
      val actorName = sender().path.name
      context.child(actorName).map {
        logger.info("graceful kill child")
        context.stop
      }
  }

  // get url and send to child
  class FetchActor extends Actor {
    private val httpFetchChild = context.actorOf(Props[HttpFetchActor](new HttpFetchActor), "httpFetchActor")
    private var retryCount = 0

    override def receive: Receive = {
      case Next =>
        //        println("send next order"+sender().path.name)
        urlActor ! GetUrl
      case url: SKURL =>
        //        println("will fetch " + url.getUrl)
        retryCount = 0
        httpFetchChild ! url
      case NoneUrl =>
        // wait and retry
        retryCount += 1
        //        if (retryCount % 10 == 0)
        //          println("==========================" + retryCount)
        if (retryCount >= 500) {
          //          println("----------------------------------")
          context.stop(self)
        } else {
          context.system.scheduler.scheduleOnce(100 millisecond, self, Next)
        }
      case Retry(url) =>
        urlActor ! url
      case _ =>
        logger.warn("unknown message from " + sender().path.name)
    }
  }

  class HttpFetchActor extends Actor {
    override def receive: Receive = {
      case skurl: SKURL =>
        val send = sender()
        //        println(skurl.getUrl + " will be fetched")
        //        context.become(proxyState(skurl))
                val proxy = ProxyPool.getProxy
//        var proxy: String = null
//        if (!skurl.isseed() && properties.isProxy) {
//          proxy = ProxyPool.getProxy
//        }
        val f = Future {
          HttpClientUtil.executeHttpGet(skurl.getUrl, userClient, proxy)
        }
        f.onComplete {
          case Success(response) =>
            //            println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^1")
            try {
              val content = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity), properties.getCharset)
              val code = ResponseUtils.getResponseStatus(response)
              skurl.setStausCode(code)
              if (code == 200) {
                skurl.setContent(content)
              }
            } catch {
              case e: Exception =>
            }
            //            println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^2")
            try {
              EntityUtils.consume(response.getEntity)
              response.close()
            } catch {
              case e: Exception =>
            }

            if (skurl.getStausCode == 200 && skurl.getContent != null && skurl.getContent.trim.length > 0) {
              extractActor ! skurl
            } else {
              //              println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^3")
            }
          case Failure(e) =>
            //            println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^4")
            //            log.warning(e.getMessage, e)
            if (skurl.shouldRetry()) {
              send ! Retry(skurl)
            }
            ProxyPool.remove(proxy)
        }
        f.onComplete {
          case _ =>
            send ! Next
//            logger.info("%%%%%%%%%%%%%%%%%%" + self.path.toString)
        }
      case _ =>
        logger.warn("unknown message from " + sender().path.name)
    }

    // if use proxy actor
    def proxyState(skurl: SKURL): Receive = {
      case Proxy(proxy) =>
        val f = Future {
          HttpClientUtil.executeHttpGet(skurl.getUrl, userClient, proxy.get)
        }
        f.onComplete {
          case Success(response) =>
            try {
              val content = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity), properties.getCharset)
              val code = ResponseUtils.getResponseStatus(response)
              skurl.setStausCode(code)
              if (code == 200) {
                skurl.setContent(content)
              }
            } catch {
              case e: Exception =>
            }
            try {
              EntityUtils.consume(response.getEntity)
              response.close()
            } catch {
              case e: Exception =>
            }

            if (skurl.getStausCode == 200 && skurl.getContent != null && skurl.getContent.trim.length > 0) {
              extractActor ! skurl
            }
          case Failure(e) =>
            logger.warn(e.getMessage, e)
        }
        f.onComplete {
          case _ =>
            context.unbecome()
        }
      case _ =>
        logger.info("unknown message from " + sender().path.name)
    }
  }

  class ExtractActor extends Actor {
    override def receive: Receive = {
      case url: SKURL =>
        //        println("^^^^^^^^^^^^^^^^^^^^^^^^5")
        Future {
          properties.getSKCJob.getExtractor.process(url, properties.getJobName)
        }
      case _ =>
        logger.info("unknown message from " + sender().path.name)
    }
  }


  class UrlPoolActor extends Actor {
    private val urls = new mutable.Queue[SKURL]

    override def receive: Receive = {
      case url: SKURL =>
        urls.enqueue(url)
        //        log.info("dashen="+urls.head.getUrl)
        if (urls.size > 100000) {
          val toFile = new util.ArrayList[String](50000)
          (0 until 50000).map {
            case _ =>
              val tmp = urls.dequeue()
              if (tmp.isseed()) {
                urls.enqueue(tmp)
              } else {
                toFile.add(tmp.getUrl)
              }
          }
          URLUtils.saveJobURL(toFile, properties.getJobName)
        }
      case GetUrl =>
        if (urls.size < 40000) {
          val list = URLUtils.loadJobURL(properties.getJobName)
          //          log.info("list size=" + list.size())
          (0 until list.size()).map {
            case index =>
              //              log.info("enqueue")
              urls.enqueue(new SKURL(list.get(index), false))
          }
          //          for (url: : list) {
          //            urls.enqueue(url)
          //          }
        }
        if (urls.isEmpty) {
          //          log.info("url is none")
          sender() ! NoneUrl
          //          println("NoneUrl")
        } else {
          //          log.info("send a url =")
          sender() ! urls.dequeue()
        }

      case Clear =>
        urls.clear()
      case _ =>
        logger.warn("unknown message from " + sender().path.name)
    }
  }

}
