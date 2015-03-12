package com.neo.sk.arachnez.commons.Objects

import java.net.InetAddress

/**
 * User: jiameng
 * Date: 2014/10/13
 * Time: 17:10
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
case class JobInfo(jobName: String = "defalt", userId: Int = 0, var lastLaunch: Long = 0l,
                   var lastActivity: Int = 2, var isDelete: Int = 0, var aliveTime: Long = 0l,
                    var ip: String = "0.0.0.0" ) {
//  var id: Int = 0
//  var ipv4 = ip
//  var jobName = name
//  var userId = uId
//  var lastLaunch = ll
//  var lastActivity = la
//  var isDelete = isDel
//  var aliveTime = aTime

  def this(name: String, userId: Int, lastLaunch: Long,
           lastActivity: Int, isDelete: Int, aliveTime: Long) = {
    this(name, userId, lastLaunch, lastActivity, isDelete, aliveTime, InetAddress.getLocalHost.getHostAddress)
  }
}
