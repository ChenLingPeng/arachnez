package com.neo.sk.arachnez.proxy;

import chen.bupt.httpclient.commons.Constants;
import chen.bupt.httpclient.handler.DefaultHttpRequestRetryHandler;
import chen.bupt.httpclient.utils.InputStreamUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/7/16
 * Time: 14:37
 */
public class ProxyTestUtil {
  private static final Logger logger = LoggerFactory.getLogger(ProxyTestUtil.class);

  private static final String index_baidu = "http://www.baidu.com/";
  private static final String index_taobao = "http://www.taobao.com/";
  private static final String index_byr = "http://bbs.byr.cn/index";
  private static final String[] indexs = {index_baidu, index_taobao, index_byr};
  private static final String[] pattern = {"http://tieba.baidu.com", "http://www.tmall.com", "http://static.byr.cn"};
  private static CloseableHttpClient checkClient;

  static {
//    NormalHttpClient client = new NormalHttpClient(105,105);
//    client.setAutoRedirection(false);
//    client.setParam(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
//    client.setParam(CoreConnectionPNames.SO_TIMEOUT, 20000);
//    checkClient = client.getCookieStore();

    MessageConstraints messageConstraints = MessageConstraints.custom()
        .setMaxHeaderCount(200)
        .setMaxLineLength(5000)
        .build();

    ConnectionConfig connectionConfig = ConnectionConfig.custom()
        .setMalformedInputAction(CodingErrorAction.IGNORE)
        .setUnmappableInputAction(CodingErrorAction.IGNORE)
        .setCharset(Consts.UTF_8)
        .setBufferSize(64 * 1024)
        .setMessageConstraints(messageConstraints)
        .build();

    RequestConfig globalConfig = RequestConfig.custom()
        .setCookieSpec(CookieSpecs.BEST_MATCH)
        .setCircularRedirectsAllowed(false)
        .setRedirectsEnabled(false)
        .setConnectTimeout(10000)
        .setSocketTimeout(10000)
//        .setConnectionRequestTimeout(10000)
        .build();

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(105);
    cm.setDefaultMaxPerRoute(105);
    cm.setDefaultConnectionConfig(connectionConfig);
    List<Header> headers = new ArrayList<>(4);
    headers.add(new BasicHeader("User-Agent", Constants.defaultUserAgent));
    headers.add(new BasicHeader("Accept", Constants.defaultAccept));
    headers.add(new BasicHeader("Accept-Encoding", Constants.defaultAcceptEncoding));
    headers.add(new BasicHeader("Accept-Language", Constants.defaultAcceptLanguage));

    checkClient = HttpClients.custom().setConnectionManager(cm)
        .setDefaultRequestConfig(globalConfig)
//        .setDefaultCookieStore(cookieStore)
        .setRetryHandler(new HttpRequestRetryHandler() {
          @Override
          public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (executionCount >= 2) {
              return false;
            }
            if (exception instanceof InterruptedIOException) {
              return false;
            }
            if (exception instanceof UnknownHostException) {
              return false;
            }
            if (exception instanceof ConnectTimeoutException) {
              return false;
            }
            if (exception instanceof SSLException) {
              return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            if (idempotent) {
              return true;
            }
            return false;
          }
        })
        .setDefaultHeaders(headers)
        .build();
  }

  public static void addTestRule(String url, String pat){
// TODO
  }

  public static boolean test(String proxy) {
    String[] tmp = proxy.split(":");
    if (tmp.length != 2) return false;
    HttpHost host = new HttpHost(tmp[0], Integer.parseInt(tmp[1]));
    RequestConfig config = RequestConfig.custom()
        .setProxy(host)
        .setConnectTimeout(10000)
//              .setConnectionRequestTimeout(10000)
        .setSocketTimeout(10000)
        .build();
    int success = 0;
    for (int i = 0; i < indexs.length; i++) {
      HttpGet get = new HttpGet(indexs[i]);
      get.setConfig(config);
      CloseableHttpResponse response = null;
      try {
        response = checkClient.execute(get);
        String content = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity()));
//        String content = InputStreamUtils.entity2String(response.getEntity());
        if (content.contains(pattern[i])) {
          success++;
        }else{
//          logger.info(proxy+" not ok");
//          logger.info(content);
        }
        EntityUtils.consume(response.getEntity());
//        get.releaseConnection();
        response.close();
        if(success>0)return true;// 放宽条件，暂时
      } catch (IOException e) {
//        logger.warn(e.getMessage());
//        logger.warn(e);
        if(response!=null){
          try {
            response.close();
          } catch (IOException e1) {
          }
        }
      }
    }
    return success > 1;
  }

  public static void main(String[] args) throws IOException {
    List<String> list = FileUtils.readLines(new File("proxy.txt"));
    for (String p : list) {
      boolean f = test(p);
      if(f){
        logger.info(p+" is OK");
      }
    }

  }
}
