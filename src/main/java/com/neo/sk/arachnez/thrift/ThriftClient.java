package com.neo.sk.arachnez.thrift;

import org.apache.commons.io.FileUtils;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import seekloud.archer.arachne.thrift.common.gencode.HDFSService;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: jiameng
 * Date: 2014/8/13
 * Time: 11:16
 */
public class ThriftClient implements HDFSService.Iface {
  private static ThriftClient instance;
  private HDFSService.Client client;

  public static String serverHost;
  public static int serverPort;
  public TTransport transport;

  private static final int maxRetryTime = 3;

  public synchronized static ThriftClient getInstance() {
    if (instance == null) {
      throw new UnsupportedOperationException("You must init BowClient first.");
    }
    return instance;
  }

  public synchronized static void init() throws TTransportException {
    if (instance != null) {
      throw new UnsupportedOperationException("Client has been init before. You can try reset(host,port) the client.");
    }
    Properties properties = new Properties();
    try {
      properties.load(ThriftClient.class.getClassLoader().getResourceAsStream("./thrift.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    reset(properties.getProperty("host","baby17"), Integer.parseInt(properties.getProperty("port","12306")));
  }

  private synchronized static void reset(String host, int port) throws TTransportException {
    if (instance != null) {
      if (instance.transport != null) {
        instance.transport.close();
      }
      instance.client = null;
      instance.transport = null;
    } else {
      instance = new ThriftClient();
    }
    serverHost = host;
    serverPort = port;
//        logger.info("client reset to " + host + ":" + port);
    instance.transport = new TSocket(host, port);
    instance.transport.open();
    TProtocol protocol = new TBinaryProtocol(instance.transport);
    instance.client = new HDFSService.Client(protocol);
  }

  private synchronized static void reset() throws TTransportException {
    if (instance == null || serverHost == null || serverPort == 0) {
      throw new UnsupportedOperationException("You must init BowClient first.");
    }
    reset(serverHost, serverPort);
  }

  private synchronized void handleTException(int tryTime, TException e) throws TException {
    if (tryTime > maxRetryTime) {
      throw e;
    }
    reset();
    try {
      this.wait(300);
    } catch (InterruptedException e1) {
//            logger.warn(e1.getMessage());
    }
  }

  private void checkConnect() throws TTransportException {
    if (!transport.isOpen()) {
//            logger.warn("transport is close, reset client.");
      reset();
    }
  }

  private static String replaceBlank(String str) {
    String dest = "";
    if (str!=null) {
      Pattern p = Pattern.compile("\r|\n");
      Matcher m = p.matcher(str);
      dest = m.replaceAll("");
    }
    return dest;
  }
  public static void main(String[] args) throws IOException {
//    System.out.println("陈凌鹏\nsrtewrt");
//    System.out.println("陈凌鹏\nsrtewrt".replaceAll("\n",""));
    System.out.println(replaceBlank(FileUtils.readFileToString(new File("d:/d.xml"))));
//    if(true)return;
    try {
      ThriftClient.init();
      ThriftClient client = ThriftClient.getInstance();
      System.out.println("test start");
      for(int i=0;i<4000;i++) {
//        client.addRecord("陈凌鹏\u0001三东方闪电" + i, "/user/hadoop/heritrixHDFSTest");
//        client.addRecord("陈凌鹏\nsrtewrt","/user/hadoop/heritrixHDFSTest");
//        client.addRecord("陈凌鹏\u0001三东方闪电" + i, "/user/hadoop/heritrixHDFSTest");
//        client.addRecord("陈凌鹏\nsrtewrt","/user/hadoop/heritrixHDFSTest");
//        client.addRecord("324234\nsrtewrt","/user/hadoop/heritrixHDFSTest");
//        client.addRecord("83718823\u0001老三，我们会\n在一起\u00012014-08-14 10:58:40\u0001设使隔壁无有吾，不知几人绿你，几人上你老婆， - 糗事百科","/user/hadoop/heritrixHDFSTest");
        client.addRecord(FileUtils.readFileToString(new File("d:/d.xml")),"/user/heritrix/douban");
      }
    } catch (TTransportException e) {
      e.printStackTrace();
    } catch (TException e) {
      e.printStackTrace();
    }
  }

  @Override
  public synchronized void addRecord(String record, String path) throws TException {
    checkConnect();
    int tryTime = 0;
    while (true) {
      try {
        client.addRecord(record, path);
        break;
      } catch (TException e) {
        tryTime++;
        handleTException(tryTime, e);
      }
    }
  }
}
