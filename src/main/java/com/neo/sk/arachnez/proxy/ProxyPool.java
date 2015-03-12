package com.neo.sk.arachnez.proxy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;


/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/7/16
 * Time: 16:56
 */
public class ProxyPool {

  private static final Logger logger = LoggerFactory.getLogger(ProxyPool.class);

  private static Queue<String> usableProxy = new ConcurrentLinkedQueue<String>();
  private static DelayQueue<DelayedObject> waitingQueue = new DelayQueue<DelayedObject>();
  // 存放当前待测和可用的proxy
  private static Map<String, Integer> allSet = new ConcurrentHashMap<String, Integer>();
  private static final long checkCycle = 60 * 60 * 1000;
  private static final long getCycle = 90 * 1000; // 代理获取周期
  private static final int waiting2test = 0;
  private static final int usable = 1;
  private static final int POOLSIZE = 1000; // 与MyProxy一致


  public static void run() {
    Timer timer = new Timer("GetProxyTask");
    GetProxyTask getTask = new GetProxyTask();
    timer.schedule(getTask, 1l, getCycle);
    logger.info("start a GetProxyTask task");

    Timer timer1 = new Timer("StateReportTask");
    StateReportTask reportTask = new StateReportTask();
    timer1.schedule(reportTask, 60 * 1000, 1000 * 60);

    startCheckTasks(100);
  }

  private static void startCheckTasks(int taskNumber) {
    Runnable checkProxyTask = new CheckProxyTask();
    for (int i = 0; i < taskNumber; i++) {
      Thread thread = new Thread(checkProxyTask, "CheckProxyTask-" + i);
      thread.start();
      //logger.info("start a checkProxyTask-" + i + " task");
    }
    //logger.info("start " + taskNumber + " check task");
  }

  public static List<String> getProxys() {
    List<String> proxys = new ArrayList<String>();
    proxys.addAll(usableProxy);
    return proxys;
  }

  public static String getProxy() {
    String proxy = usableProxy.poll();
    if (proxy != null) {
      usableProxy.offer(proxy);
    }
    return proxy;
  }

  public static void remove(String proxy) {
    usableProxy.remove(proxy);
    allSet.remove(proxy);
  }

  public static class GetProxyTask extends TimerTask {
    @Override
    public void run() {
      logger.info("start fetching proxy");
      if (ProxyPool.allSet.size() > 200) {
        return;
      }
      logger.info("need fetch...");
      Set<String> proxys = new HashSet<>();
      try {
        proxys = ProxyGetUtil.getProxy();
      } catch (Exception e) {
        logger.warn(e.getMessage(), e);
      }
      logger.info("add proxys from getproxytask with size: " + proxys.size());
      int cnt = 0;
      for (String proxy : proxys) {
        // 未见过
        if (!allSet.containsKey(proxy)) {
          cnt++;
          allSet.put(proxy, waiting2test);
          waitingQueue.add(new DelayedObject(proxy, 1l, TimeUnit.SECONDS));
        }
      }
      logger.info("proxy with size: " + cnt + " has been added to waiting list");
    }
  }

  public static class CheckProxyTask implements Runnable {
    @Override
    public void run() {
      while (true) {
        String proxy = null;
        try {
          proxy = waitingQueue.take().getProxy();
          allSet.remove(proxy);
//          logger.info("will test "+proxy);
          boolean flag = false;
          try {
            flag = ProxyTestUtil.test(proxy);
          } catch (Exception e) {
            logger.warn(e.getMessage(), e);
          }
          if (flag) {
              System.out.println(proxy);
            allSet.put(proxy, usable);
            usableProxy.offer(proxy);
//            logger.info("proxy: " + proxy + " add to usableProxyPool");
            while (usableProxy.size() > POOLSIZE) {
              String p = usableProxy.poll();
              allSet.put(p, waiting2test);
              waitingQueue.add(new DelayedObject(p, 1l, TimeUnit.SECONDS));
//              logger.info("proxy: " + p + " state change from usable to waiting2test");
            }
          } else {
//            logger.info(proxy + " not usable");
          }
        } catch (InterruptedException e) {
          logger.warn(e.getMessage());
        }
      }
    }
  }

  public static class StateReportTask extends TimerTask {
    @Override
    public void run() {
      logger.info("usable proxy number: " + usableProxy.size());
      logger.info("waiting proxy number: " + waitingQueue.size());
      logger.info("all proxy number: " + allSet.size());
      if (usableProxy.size() + waitingQueue.size() != allSet.size()) {
        logger.info("proxy number un-correct");
        allSet.clear();
        for (String proxy : usableProxy) {
          allSet.put(proxy, usable);
        }
        for (DelayedObject proxy : waitingQueue) {
          allSet.put(proxy.getProxy(), waiting2test);
        }
      }
    }
  }

  public static void main(String[] args) {
    ProxyPool.run();
//    Queue<String> q = new ConcurrentLinkedQueue<>();
//    q.add("1");
//    q.add("2");
//    q.add("3");
//    logger.info(q.size());
//    q.remove("2");
//    logger.info(q.size());
//    q.remove("4");
//    logger.info(q.size());
  }


}