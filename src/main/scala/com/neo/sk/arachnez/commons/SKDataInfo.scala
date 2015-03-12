package com.neo.sk.arachnez.commons

import java.sql.SQLException

import akka.actor.{Actor, ActorLogging}
import com.neo.sk.arachnez.util.DbUtil
import org.apache.log4j.Logger

/**
 * User: jiameng
 * Date: 2014/10/14
 * Time: 19:59
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
case class SKDataInfo(jobName: String) {
  private val logger: Logger = Logger.getLogger(classOf[SKDataInfo])

  def insertDataByTime(count: Long, startTime: Long, endTime: Long) {
    try {
      if (startTime != 0) {
        val time: Long = System.currentTimeMillis
        val sql = "Insert into ara_everytime (e_jobname,e_count,e_starttime,e_endTime) values(?,?,?,?)"
        val params: Array[Any] = Array(jobName, count, startTime, endTime)
        DbUtil.update(sql, params.map{param => param.asInstanceOf[AnyRef]}: _*)
      }
    }
    catch {
      case e: SQLException => logger.warn(e.getMessage, e)
    }
  }
}
