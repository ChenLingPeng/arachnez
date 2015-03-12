package com.neo.sk.arachnez.commons

import java.sql.SQLException
import java.util.Calendar
import java.util.concurrent.{Executors, TimeUnit}

import akka.actor.Actor.Receive
import akka.actor.{ActorSystem, Props, ActorLogging, Actor}
import org.joda.time.{Seconds, DateTime}
import org.slf4j.{LoggerFactory, Logger}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import com.neo.sk.arachnez.commons.Objects.JobInfo
import com.neo.sk.arachnez.framework._
import com.neo.sk.arachnez.util.DbUtil

/**
 * User: jiameng
 * Date: 2014/10/13
 * Time: 14:15
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
class SKJobInfoActor(jobName: String) extends Actor{
  private val logger: Logger = LoggerFactory.getLogger(classOf[SKJobInfoActor])
  private val insertJobActor = context.actorOf(Props[InsertJobActor](new InsertJobActor), "insertJobActor")
  private val insertByDayActor = context.actorOf(Props[InsertByDayActor](new InsertByDayActor), "insertByDayActor")
  private val heartBeatAcotr = context.actorOf(Props[HeartBeatAcotr](new HeartBeatAcotr), "heartBeatAcotr")
  private val jobInfo: JobInfo = new JobInfo(jobName, 0, 0, 2, 0, 0)

  implicit val Exe = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
  //统计日数据定时
  val stat = context.system.scheduler.schedule(
    Duration.create(nextExecutionInSeconds(23, 59), TimeUnit.SECONDS),
      Duration.create(24, TimeUnit.HOURS),
  insertByDayActor, InsertDataByDay)

  //心跳定时
  val heart = context.system.scheduler.schedule(0 seconds, 10 seconds, heartBeatAcotr, HeartBeat)

  override def receive: Receive = {
    case msg => insertJobActor ! msg
//    case _ => log.info("wrong command")
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    logger.info("jobinfoactor stoped!")
    logger.info("stat and heart will canceled!")
    stat.cancel()
    heart.cancel()
  }

  /**
   * 主要用于插入job，以及更新Job当前状态
   * @param job
   */
  def updateJob(job: JobInfo) = {
    try {
      this.synchronized{
        if (isJobExist(job.jobName)) {
          val sql: String = "Update arachnez_jobs set job_lastLaunch=?," + "job_lastActivity=? ,job_alivetime=? where job_name= ?"
          val params: Array[Any] = Array(job.lastLaunch, job.lastActivity, job.aliveTime, job.jobName)

          DbUtil.update(sql, params.map{param => param.asInstanceOf[AnyRef]}: _*)
        }
        else {
          val sql: String = "Insert into arachnez_jobs(job_ip,job_name,job_userid,job_lastActivity,job_isdelete,job_alivetime) values(?,?,?,?,?,?)"
          val params: Array[Any] = Array(job.ip, job.jobName, job.userId, job.lastActivity, job.isDelete, job.aliveTime)
          //        val params1: java.util.Arrays[Object] = params
          //        val params: Array[Any] = Array(job.ip, job.jobName, job.userId, job.lastActivity, job.isDelete, job.aliveTime)
          //        println("===========")
          //        params.foreach(e => println(e.toString))
          DbUtil.update(sql, params.map{param => param.asInstanceOf[AnyRef]}: _*)
        }
      }
    }
    catch {
      case e: SQLException => {
        logger.warn(e.getMessage)
      }
    }
  }

  def insertDataByDay(jobName: String, count: Long, date: Long): Unit = {
    try {
      val sql: String = "Insert into ara_everyday (e_jobname,e_count,e_time) values(?,?,?)"
      val params: Array[Any] = Array(jobName, count, date)
      DbUtil.update(sql, params.map{param => param.asInstanceOf[AnyRef]}: _*)
    }
    catch {
      case e: SQLException => {
        logger.warn(e.getMessage, e)
      }
    }
  }

  /**
   * 根据jobName和ip确定唯一job
   * @param jobName
   * @return
   */
  def isJobExist(jobName: String): Boolean = {
    val sql = "select * from arachnez_jobs where job_name = " + "'" + jobName + "'"
    val list = DbUtil.selectArrayList(sql)
    list != null && list.size != 0
  }

  def count(startTime: Long, endTime: Long, jobName: String): Long = {
    try {
      val sql = "select SUM(e_count) from ara_everytime " + "where e_jobname = ? and e_starttime >= ? and e_endtime <= ?"
      val params: Array[Any] = Array(jobName, startTime, endTime)
      val list = DbUtil.selectArrayList(sql, params.map{param => param.asInstanceOf[AnyRef]}: _*)
      if (list != null && list.size != 0) list.get(0)(0).toString.toLong else 0l;
    } catch {
      case e: SQLException => {
        logger.warn(e.getMessage, e)
        0l
      }
    }
  }

  def getStartTime: Long = {
    var calendar = Calendar.getInstance()
    calendar.set(Calendar.AM_PM, Calendar.AM)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val time = calendar.getTime.getTime
    time
  }

  def getEndTime: Long = {
    var calendar = Calendar.getInstance()
    calendar.set(Calendar.AM_PM, Calendar.PM)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val time = calendar.getTime.getTime
    time
  }

  def nextExecutionInSeconds(hour: Int, minute: Int): Int = {
    Seconds.secondsBetween(
      new DateTime(),
      nextExecution(hour, minute)
    ).getSeconds()
  }

  def nextExecution(hour: Int, minute: Int): DateTime = {
    val next = new DateTime()
      .withHourOfDay(hour)
      .withMinuteOfHour(minute)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)
    if (next.isBeforeNow()) next.plusHours(24) else next
  }

  class InsertJobActor extends Actor {
    override def receive: Receive = {
      case InitJobInfo => {
        jobInfo.lastActivity = 2
        jobInfo.lastLaunch = 0l
        updateJob(jobInfo)
      }

      case Run => {
        jobInfo.lastActivity = 0
        jobInfo.lastLaunch = System.currentTimeMillis()
        updateJob(jobInfo)
      }

      case Sleep => {
        jobInfo.lastActivity = 1
        updateJob(jobInfo)
      }

      case Stop => {
        jobInfo.lastActivity = 2
        updateJob(jobInfo)
      }

      case _ => logger.info("wrong command")
    }
  }

  class InsertByDayActor extends Actor {
    override def receive: Receive = {
      case InsertDataByDay => {
        val start = getStartTime
        val end = getEndTime
        logger.info("statistic day data start")
        val c = count(start, end, jobName)
        insertDataByDay(jobName, c, end)
      }

      case _ => logger.info("wrong command")
    }
  }

  class HeartBeatAcotr extends Actor {
    override def receive: Receive = {
      case HeartBeat => {
        val date = System.currentTimeMillis()
        logger.info(jobInfo.jobName + "-send a heart beat to job table")
        jobInfo.aliveTime = date
        updateJob(jobInfo)
      }
      case _ => logger.info("wrong command")
    }
  }

}
