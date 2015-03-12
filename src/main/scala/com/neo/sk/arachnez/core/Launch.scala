package com.neo.sk.arachnez.core

import akka.actor.{Props, ActorSystem}
import com.neo.sk.arachnez.framework.SKControllerActor
import com.neo.sk.arachnez.proxy.ProxyPool
import com.neo.sk.arachnez.thrift.{TritonClient, ThriftClient}
import com.neo.sk.arachnez.util.{URLUtils, DbUtil}

/**
 * User: jiameng
 * Date: 2014/10/15
 * Time: 15:45
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
object Launch extends App{
  DbUtil.init("c3p0.properties")
  ProxyPool.run()
//  ThriftClient.init()
  TritonClient.init()
  URLUtils.clearAllURL()
  val system = ActorSystem("arachenz")
  def apply() = system
  val controller = system.actorOf(Props[SKControllerActor](new SKControllerActor), "controller")
}
