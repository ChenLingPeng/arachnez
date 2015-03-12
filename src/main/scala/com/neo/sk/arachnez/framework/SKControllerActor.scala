package com.neo.sk.arachnez.framework

import java.io.File
import java.util.concurrent.Executors

import akka.actor.{Props, Actor, ActorLogging, ActorSystem}
import com.neo.sk.arachnez.commons.SKProperties
import org.slf4j.{LoggerFactory, Logger}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.collection.mutable

/**
 * User: jiameng
 * Date: 2014/10/11
 * Time: 15:23
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃
 * 　　┃　　　┃
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 */
class SKControllerActor extends Actor {
  private val logger: Logger = LoggerFactory.getLogger(classOf[SKControllerActor])
  implicit val Exe = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
  private val scanActor = context.actorOf(Props[ScanActor](new ScanActor), "scanactor")
  //扫描目录
//  private val addActor = context.actorOf(Props[AddActor](new AddActor), "addactor") //添加Job
//  private val skEngine = context.actorOf(Props[SKEngineActor](new SKEngineActor), "skengineactor")

  private var jobList = List[String]()
  private var lastModiMap = mutable.HashMap[String, String]()
  private var jobsPath = System.getProperty("user.dir") + "/jobs" //E:\Arachnez/jobs
  context.system.scheduler.schedule(5 seconds, 120 seconds, self, ScanDir)

  override def receive: Receive = {
    case ScanDir =>{
      logger.info("start scan")
      scanActor ! ScanDir
    }
    case _ => logger.warn("wrong command")
  }

  def scan(jobsPath: String): Unit = {
    val jobsDir = new java.io.File(jobsPath) //E:\Arachnez/jobs
    if (jobsDir.exists()) {
      for (jobDir <- jobsDir.listFiles()) {
        try {
          var flag = 0
          var total: String = ""
          var seedsPath = jobDir.getPath() + "/seeds.txt";
          for (file <- jobDir.listFiles()) {
            if (file.getName().equals("seeds.txt")) {
              total = total + file.lastModified().toString
              flag += 1
            } else if (file.getName().equals("skspider.properties")) {
              total = total + file.lastModified().toString
              flag += 1
            } else if (file.getName().contains(".jar")) {
              total = total + file.lastModified().toString
              flag += 1
            }
          }
          if (flag == 3){
            if(lastModiMap.isDefinedAt(jobDir.getName()) && lastModiMap.get(jobDir.getName()).get.equals(total)){
              //do nothing
            } else {
                var sp: SKProperties = new SKProperties(jobDir.getPath())
                var sf: File = new File(seedsPath)
                if (sp.isUsable() && sf.exists()){
                  lastModiMap.put(jobDir.getName(), total)
                  SKEngine() ! AddJob(sf, sp)
                  logger.info("This jobdir[" + jobDir.getName() + "] is modified and add it to SKEngine");
                }else if(!sp.isUsable() || !sf.exists()){
                  SKEngine() ! KillJob(jobDir.getName())
                  lastModiMap.put(jobDir.getName(), total)
                  logger.info("This jobdir[" + jobDir.getName() + "] is modified and is Unusable, will kill it");
                }
            }
          }
        } catch {
          case ex: Exception =>
          {
            println("------------------------------"+ex.getMessage+"------------------")
            logger.warn("scan error")
          }
        }
      }
    }
  }

  class ScanActor extends Actor {
    override def receive: Receive = {
      case ScanDir => {
        logger.info("scan job dir")
        scan(jobsPath)
      }
      case _ => logger.warn("wrong command")
    }
  }

//  class AddActor extends Actor {
//    override def receive: Receive = {
//      case AddJob(seed, prop) => {
//        log.info("add a new job")
//        skEngine ! AddJob(seed, prop)
//      }
//      case _ => log.warning("wrong command")
//    }
//  }

}



