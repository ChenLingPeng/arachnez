package com.neo.sk.arachnez.thrift;

import com.neo.triton.thrift.gencode.ExecResponse;
import com.neo.triton.thrift.gencode.TritonService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: jiameng
 * Date: 2014/12/9
 * Time: 15:08
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
public class TritonClient implements TritonService.Iface {
  private static TritonClient instance;
  private TritonService.Client client;

  public static String serverHost;
  public static int serverPort;
  public TTransport transport;

  private static final int maxRetryTime = 3;

  public synchronized static TritonClient getInstance() {
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
      properties.load(TritonClient.class.getClassLoader().getResourceAsStream("./thrift.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    reset(properties.getProperty("host", "angel0"), Integer.parseInt(properties.getProperty("port", "30101")));
  }

  private synchronized static void reset(String host, int port) throws TTransportException {
    if (instance != null) {
      if (instance.transport != null) {
        instance.transport.close();
      }
      instance.client = null;
      instance.transport = null;
    } else {
      instance = new TritonClient();
    }
    serverHost = host;
    serverPort = port;
//        logger.info("client reset to " + host + ":" + port);
    instance.transport = new TSocket(host, port);
    instance.transport.open();
    TProtocol protocol = new TBinaryProtocol(instance.transport);
    instance.client = new TritonService.Client(protocol);
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
    if (str != null) {
      Pattern p = Pattern.compile("\r|\n");
      Matcher m = p.matcher(str);
      dest = m.replaceAll("");
    }
    return dest;
  }

//  @Override
//  public synchronized void addRecord(String record, String path) throws TException {
//    checkConnect();
//    int tryTime = 0;
//    while (true) {
//      try {
//        client.addRecord(record, path);
//        break;
//      } catch (TException e) {
//        tryTime++;
//        handleTException(tryTime, e);
//      }
//    }
//  }

  @Override
  public synchronized ExecResponse addRecord(String record, String station) throws TException {
    checkConnect();
    int tryTime = 0;
    while (true) {
      try {
        ExecResponse rst = client.addRecord(record, station);
        return rst;
//        break;
      } catch (TException e) {
        tryTime++;
        handleTException(tryTime, e);
      }
    }
  }

  @Override
  public synchronized ExecResponse addRecords(List<String> records, String station) throws TException {
    checkConnect();
    int tryTime = 0;
    while (true) {
      try {
        ExecResponse rst = client.addRecords(records, station);
        return rst;
//        break;
      } catch (TException e) {
        tryTime++;
        handleTException(tryTime, e);
      }
    }
  }
}
