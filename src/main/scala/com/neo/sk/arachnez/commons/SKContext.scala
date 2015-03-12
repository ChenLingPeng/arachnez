package com.neo.sk.arachnez.commons

import java.io._
import java.util
import java.util.concurrent.ConcurrentHashMap

import org.slf4j.{LoggerFactory, Logger}
import scala.collection.JavaConverters._
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.mutable
import scala.io.Source
import scala.collection.mutable.Map

/**
 * User: jiameng
 * Date: 2014/10/14
 * Time: 19:58
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
case class SKContext(fileName: String) {
  private val logger: Logger = LoggerFactory.getLogger(classOf[SKContext])
  private val MAXFILE = fileName
  private var basePath = System.getProperty("user.dir")

  /**
   * 转换成java map
   * @return
   */
  def GetOldMaxArticleList: util.Map[String, Object] = {
    GetOldMaxArticleListFromFile.asJava
  }

  private def GetOldMaxArticleListFromFile(): mutable.HashMap[String, Object] = {
    var maxList = new mutable.HashMap[String, Object]()
    try {
      val file = new File(basePath + "/jobs/" + MAXFILE) //获取保存各类最大文章id的文件
      if(file.exists()){
        val lines = Source.fromFile(file).getLines().toList
        (0 until lines.size).map{
          case index => maxList.put(lines(index).split(":")(0), lines(index).split(":")(1))
        }
      }else{
        logger.info(basePath + "/jobs/" + MAXFILE + "- is not exists")
        file.createNewFile()
      }
    } catch {
      case e: IOException => logger.warn(e.getMessage(), e)
    }
    maxList
  }

  def saveMaxList(newMaxArticleList: util.Map[String, Object]) {
    logger.info("set MaxId list to " + fileName)
    val g: Map[String, Object] = newMaxArticleList//java map to scala map
    writeIdToFile(g)
  }

  def writeIdToFile(maxlist: Map[String, Object]) {
    try {
      val file: File = new File(basePath + "/jobs/" + MAXFILE)//获取保存各类最大文章id的文件
      if (file.exists()) {
        val writer: BufferedWriter = new BufferedWriter(new FileWriter(file))
//        StringBuffer sb = new StringBuffer("")
//        for (String category : maxlist.keySet()) {
//          sb.append(category).append(":").append(String.valueOf(maxlist.get(category))).append("\n")
//        }
        maxlist.foreach(e => {
          val (key, value) = e
          writer.write(key + ":" + value.toString + "\n")
        })
        writer.close()
      }
    } catch {
      case e: IOException => logger.warn(e.getMessage(), e)
    }
  }
}
