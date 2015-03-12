package com.neo.sk.arachnez.commons

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter, File}
import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import akka.actor.{ActorLogging, Actor}
import akka.actor.Actor.Receive
import com.neo.sk.arachnez.framework.{AddItem, Flush}
import org.slf4j.{LoggerFactory, Logger}

/**
 * User: jiameng
 * Date: 2014/10/14
 * Time: 20:00
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
case class SKStatistic(jobName: String) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[SKStatistic])
  private val statistics =  "statistics" //统计文件名
  private val data2Sql = new SKDataInfo(jobName)

  private var downSum: Long = 0L //全部下载总数
  private var itemSum: Long = 0L //全部增量总数
  private var downAdd: Long = 0L //一次下载数
  private var itemAdd: Long = 0L //一次增量数
  private var startTime: Long = 0l //每次爬取开始时间
  private var endTime: Long = 0l //每次爬取结束时间
  private var flag: Boolean = true //每次开始爬取和结束的标志位，true代表开始新一轮爬取

  private def statiItems(filePath: String) = {
    downSum = downSum + downAdd
    itemSum = itemSum + itemAdd
    if (startTime == 0l) {
      startTime = System.currentTimeMillis
    }
    endTime = System.currentTimeMillis
    data2Sql.insertDataByTime(itemAdd, startTime, endTime)
    outputStatic(filePath)
    downAdd = 0l
    itemAdd = 0l
    startTime = 0l
    endTime = 0l
    flag = true
  }

  private def outputStatic(filePath: String) = {
    try {
      val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      df.setTimeZone(TimeZone.getTimeZone("GMT+8"))
      val f: File = new File(filePath)
      if (!f.exists())
        f.mkdirs()
      val  osw: OutputStreamWriter = new OutputStreamWriter(
        new FileOutputStream(filePath + statistics, true), "UTF-8")
      val writer: BufferedWriter = new BufferedWriter(osw)
      writer.write(df.format(new Date()) + " # " + downSum + " # " + itemSum
        + " # " + downAdd + " # " + itemAdd + "\n")
      writer.close()
    } catch {
      case e: Exception => logger.warn(e.getMessage(), e)
    }
  }

  def flushData(filePath: String) = {
    this.synchronized{
      logger.info(jobName + " - flush statistic data!");
      statiItems(filePath)
    }
  }

  def addItems(down: Long, size: Long) = {
    try {
      this.synchronized{
        if (flag) {//如果是true，说明新一轮爬取刚开始，那么可以插入数据
          logger.info("new loop, time init")
          startTime = System.currentTimeMillis()
          flag = false
        }
        downAdd = downAdd + down
        itemAdd = itemAdd + size
      }
    } catch {
      case e: Exception => logger.warn(e.getMessage())

    }
  }


//  class FlushActor(filePath: String) extends Actor{
//    override def receive: Receive = {
//      case Flush => logger.info("flush statistic data!")
//        statiItems(filePath)
//    }
//  }

//  class AddActor(down: Long, size: Long) extends Actor{
//    override def receive: Receive = {
//      case AddItem => {
//        try{
//          if(flag){
//            logger.info("new loop, time init")
//            startTime = System.currentTimeMillis()
//            flag = false
//          }
//          downAdd += down
//          itemAdd += size
//        }catch {
//          case e => logger.warn(e.getMessage, e)
//        }
//      }
//    }
//  }
}
