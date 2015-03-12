package com.neo.sk.arachnez.proxy;

import chen.bupt.httpclient.NormalHttpClient;
import chen.bupt.httpclient.method.CHttpGet;
import chen.bupt.httpclient.utils.InputStreamUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/7/16
 * Time: 12:57
 */
public class ProxyGetUtil {


  private static final Logger logger = LoggerFactory.getLogger(ProxyGetUtil.class);

  private static DefaultHttpClient httpClient;

  private static boolean first = true;

  static {
    NormalHttpClient normalHttpClient = new NormalHttpClient();
    normalHttpClient.setParam(CoreConnectionPNames.SO_TIMEOUT, 50000);
    normalHttpClient.setParam(CoreConnectionPNames.CONNECTION_TIMEOUT, 50000);
    httpClient = normalHttpClient.getHttpClient();
  }

  private static final String pachong_urls[] = {
      "http://pachong.org/anonymous.html",
      "http://pachong.org",
      "http://pachong.org/transparent.html"
  };
  private static final String hidemyass_url = "http://hidemyass.com/proxy-list/";
  private static final String freeproxy_url = "http://free-proxy-list.net/";
  private static final String spysru_url = "http://spys.ru/en/http-proxy-list/";
  private static final String samair_url[] = {
      "http://www.samair.ru/proxy/proxy-01.htm",
      "http://www.samair.ru/proxy/proxy-02.htm",
      "http://www.samair.ru/proxy/proxy-03.htm",
      "http://www.samair.ru/proxy/proxy-04.htm",
      "http://www.samair.ru/proxy/proxy-05.htm",
      "http://www.samair.ru/proxy/proxy-06.htm",
      "http://www.samair.ru/proxy/proxy-07.htm",
      "http://www.samair.ru/proxy/proxy-08.htm",
      "http://www.samair.ru/proxy/proxy-09.htm",
      "http://www.samair.ru/proxy/proxy-10.htm",
      "http://www.samair.ru/proxy/proxy-11.htm",
      "http://www.samair.ru/proxy/proxy-12.htm",
      "http://www.samair.ru/proxy/proxy-13.htm",
      "http://www.samair.ru/proxy/proxy-14.htm",
      "http://www.samair.ru/proxy/proxy-15.htm",
      "http://www.samair.ru/proxy/proxy-16.htm",
      "http://www.samair.ru/proxy/proxy-17.htm",
      "http://www.samair.ru/proxy/proxy-18.htm",
      "http://www.samair.ru/proxy/proxy-19.htm",
      "http://www.samair.ru/proxy/proxy-20.htm",
      "http://www.samair.ru/proxy/proxy-21.htm",
      "http://www.samair.ru/proxy/proxy-22.htm",
      "http://www.samair.ru/proxy/proxy-23.htm",
      "http://www.samair.ru/proxy/proxy-24.htm",
      "http://www.samair.ru/proxy/proxy-25.htm",
      "http://www.samair.ru/proxy/proxy-26.htm",
      "http://www.samair.ru/proxy/proxy-27.htm",
      "http://www.samair.ru/proxy/proxy-28.htm",
      "http://www.samair.ru/proxy/proxy-29.htm",
      "http://www.samair.ru/proxy/proxy-30.htm"
  };

  private static final String[] proxycom = {
      "http://proxy.com.ru/list_1.html",
      "http://proxy.com.ru/list_2.html",
      "http://proxy.com.ru/list_3.html",
      "http://proxy.com.ru/list_4.html",
      "http://proxy.com.ru/list_5.html",
      "http://proxy.com.ru/list_6.html",
      "http://proxy.com.ru/list_7.html",
      "http://proxy.com.ru/list_8.html",
      "http://proxy.com.ru/list_9.html",
      "http://proxy.com.ru/list_10.html",
      "http://proxy.com.ru/list_11.html",
      "http://proxy.com.ru/list_12.html",
      "http://proxy.com.ru/list_13.html",
      "http://proxy.com.ru/list_14.html",
      "http://proxy.com.ru/list_15.html",
      "http://proxy.com.ru/list_16.html",
      "http://proxy.com.ru/list_17.html",
      "http://proxy.com.ru/list_18.html",
      "http://proxy.com.ru/list_19.html",
      "http://proxy.com.ru/list_20.html"
  };

  private static void updateProxy() {
    Set<String> proxys = new HashSet<String>();
    proxys.addAll(getProxyListFromPaChongPage());
    for (String p : proxys) {
      System.out.println(p);
    }
    proxys.clear();
    System.out.println(2);
    proxys.addAll(getProxyListFromFreeProxyListPage());
    for (String p : proxys) {
      System.out.println(p);
    }
    proxys.clear();
    System.out.println(3);
//    proxys.addAll(getProxyListFromSamairPage());
    for (String p : proxys) {
      System.out.println(p);
    }
    proxys.clear();
    System.out.println(4);

//		Set<String> list = ProxySource.loadProxyList(Constants.basePath + "/"
//				+ Constants.newProxyFile);
//		if (list != null)
//			proxys.addAll(list);
//    try {
//      System.out.println("write to: "+Constants.newProxyFile);
//      FileUtils.writeLines(new File(Constants.basePath + "/"
//          + Constants.newProxyFile), proxys);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }

  private static Set<String> getProxyListFromPaChongPage() {
    Set<String> proxys = new HashSet<String>();
    for (String url : pachong_urls) {
      Document doc = null;
      CHttpGet cHttpGet = new CHttpGet(url);
      try {
        HttpResponse response = httpClient.execute(cHttpGet.getHttpGet());
        String content = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity()));
        if (content != null && !"".equals(content)) {
          doc = Jsoup.parse(content);
        }
      } catch (IOException e) {
      }
      cHttpGet.getHttpGet().releaseConnection();
//      try {
//        doc = Jsoup.connect(url).timeout(20000).get();
//      } catch (IOException e) {
//        // TODO Auto-generated catch block
//        continue;
//      }
      String head = doc.head().toString();
      int ind = head.indexOf("<script type=\"text/javascript\">var");
      head = head.substring(ind + 31);
      ind = head.indexOf("</script>");
      head = head.substring(0, ind);
//      logger.info(head);
      Map<String, Integer> ans = parseJS(head);
//      logger.info(ans);
      Elements eles = doc.getElementsByTag("tr");
      for (Element ele : eles) {
        Elements tmps = ele.getElementsByTag("td");
//        logger.info(tmps.size());
        if (tmps.size() != 7) {
//          logger.info(ele.text());
          continue;
        }
        if (tmps.get(4).text().contains("anonymous")
            || tmps.get(4).text().contains("high")
            || tmps.get(4).text().contains("transparent")) {
          String js = tmps.get(2).html();
          int i = js.indexOf('(');
          int j = js.lastIndexOf(')');
          js = js.substring(i + 1, j);
          proxys.add(tmps.get(1).text() + ":" + eval2(js, ans));
        }
      }
    }
//    logger.info(proxys.size());
    return proxys;
  }

  // http://hidemyass.com/proxy-list/
  private static Set<String> getProxyListFromHidemyassPage() {
    Set<String> proxys = new HashSet<String>();

    String url = hidemyass_url;
    Document doc = null;
    try {
      doc = Jsoup.connect(url).timeout(10000).get();
    } catch (IOException e) {
      e.printStackTrace();
    }
    ;
    Elements eles = doc.getElementsByTag("tr");
    for (Element ele : eles) {
      Elements tmps = ele.getElementsByTag("td");
      if (tmps.size() != 8)
        continue;
      if (tmps.get(6).text().equalsIgnoreCase("HTTP")) {
        proxys.add(tmps.get(1).text() + ":" + tmps.get(2).text());
        System.out.println(tmps.get(1).text() + ":"
            + tmps.get(2).text());
      }
    }

    return proxys;
  }

  // http://free-proxy-list.net/
  private static Set<String> getProxyListFromFreeProxyListPage() {
    Set<String> proxys = new HashSet<String>();
    String url = freeproxy_url;

    Document doc = null;
    CHttpGet cHttpGet = new CHttpGet(url);
    try {
      HttpResponse response = httpClient.execute(cHttpGet.getHttpGet());
      String content = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity()));
      if (content != null && !"".equals(content)) {
        doc = Jsoup.parse(content);
      }
    } catch (IOException e) {
    }
    cHttpGet.getHttpGet().releaseConnection();
//    try {
//      doc = Jsoup.connect(url).timeout(20000).get();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    if(doc==null) return proxys;
    Elements eles = doc.getElementsByTag("tr");
    for (Element ele : eles) {
      Elements tmps = ele.getElementsByTag("td");
      if (tmps.size() != 8)
        continue;
      proxys.add(tmps.get(0).text() + ":" + tmps.get(1).text());
    }
    return proxys;
  }

  // http://spys.ru/en/http-proxy-list/
  private static Set<String> getProxyListFromSpysruPage() {
    Set<String> proxys = new HashSet<String>();
    for (int i = 0; i < 2; i++) {
      String url = spysru_url + i + "/";
      Document doc = null;
      try {
        doc = Jsoup.connect(url).timeout(5000).get();
      } catch (IOException e) {
        continue;
      }
      Elements eles = doc.getElementsByTag("tr");
      for (Element ele : eles) {
        Elements tmps = ele.getElementsByTag("td");
        if (tmps.size() != 6)
          continue;
        System.out.println(tmps.text());
        if (tmps.get(1).text().equalsIgnoreCase("HTTP")) {
          Elements tmpEs = tmps.get(0).getElementsByClass("spy14");
          if (tmpEs != null && tmpEs.size() == 1) {
            System.out.println(tmpEs.html());
            proxys.add(tmpEs.text());
          }

        }
      }
    }
    return proxys;
  }
/*

  private static Set<String> getProxyListFromSamairPage() {
    Set<String> proxys = new HashSet<String>();
    String url = samair_url;
    Document doc = null;
    try {
      doc = Jsoup.connect(url).timeout(20000).get();
    } catch (IOException e) {
      System.err.println("timeout1");
    }

    if (doc != null) {
      Elements eles = doc.getElementsByClass("page");
      for (Element ele : eles) {
        String tmp = ele.attr("href");
        // System.out.println("---" + tmp + "---");
        if (tmp.contains("proxy-1.htm"))
          tmp = "proxy-01.htm";
        Set<String> tmpp = getPureProxy("http://www.samair.ru/proxy/"
            + tmp);
        if (tmpp != null && tmpp.size() > 0)
          proxys.addAll(tmpp);
      }
    }
//    System.out.println("proxy size: " + proxys.size());
    return proxys;
  }
*/

  private static String getPureAdd(Document doc) {
    Element e = doc.getElementById("ipportonly");
    if (e == null) return null;
    Elements eles = e.getElementsContainingText("these proxies");
    if (eles != null && eles.size() > 0) {
      String dirUrl = eles.attr("href");
      return dirUrl;
    }
    return null;
  }

  private static Set<String> getPureProxy(String url) {
    Set<String> pp = new HashSet<String>();
    Document doc = null;
    CHttpGet cHttpGet = new CHttpGet(url);
    try {
      HttpResponse response = httpClient.execute(cHttpGet.getHttpGet());
      String content = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity()));
      if (content != null && !"".equals(content)) {
        doc = Jsoup.parse(content);
      }
    } catch (IOException e) {
    }
    cHttpGet.getHttpGet().releaseConnection();
//    try {
//      doc = Jsoup.connect(url).timeout(20000).get();
//    } catch (IOException e) {
//      System.err.println("timeout2");
//    }
    // Element e = doc.getElementById("ipportonly");
    if (doc == null)
      return pp;
    String dirUrl = getPureAdd(doc);
    if (dirUrl == null) return pp;
//    try {
//      doc = Jsoup.connect("http://www.samair.ru" + dirUrl).timeout(20000)
//          .get();
//    } catch (IOException e2) {
//      System.err.println("timeout3");
//    }
    doc = null;

    CHttpGet cHttpGet2 = new CHttpGet("http://www.samair.ru" + dirUrl);
    try {
      HttpResponse response = httpClient.execute(cHttpGet2.getHttpGet());
      String content = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity()));
      if (content != null && !"".equals(content)) {
        doc = Jsoup.parse(content);
      }
    } catch (IOException e) {
    }
    cHttpGet2.getHttpGet().releaseConnection();


    if (doc == null)
      return pp;
    Element ele = doc.getElementById("content");
    // System.out.println(ele.text());
    for (String s : ele.text().split("\n")) {
      pp.add(s);
    }
//    logger.info(pp.size());
    return pp;
  }

  public static Set<String> getPureProxyList() {
    Set<String> res = new HashSet<>();
    for (String url : samair_url) {
      res.addAll(getPureProxy(url));
    }
    return res;
  }

  public static Set<String> getProxycomList(String url) {
    Document doc = null;
    CHttpGet cHttpGet = new CHttpGet(url);
    try {
      HttpResponse response = httpClient.execute(cHttpGet.getHttpGet());
      String content = EntityUtils.toString(InputStreamUtils.getRealEntity(response.getEntity()),"gb2312");
      if (content != null && !"".equals(content)) {
        doc = Jsoup.parse(content);
      }
    } catch (IOException e) {
    }
    Set<String> res = new HashSet<>();
    try {
      cHttpGet.getHttpGet().releaseConnection();
      if (doc == null) {
        return res;
      }
      Elements eles = doc.select("tbody");
      for (Element ele : eles) {
        Elements tmps = ele.select("tr");
//      logger.info(tmps.size());
        if (tmps.size() != 51) continue;
//      logger.info("process");
        for (int i = 1; i < tmps.size(); i++) {
          Element tmp = tmps.get(i);
//        logger.info(tmp.text());
          Elements items = tmp.select("td");
          if (items.size() == 5) {
            res.add(items.get(1).text() + ":" + items.get(2).text());
          } else {
            logger.info("error");
          }
        }
      }
    } catch (Exception e) {
    }
    return res;
  }

  public static Set<String> getAllProxycom() {
    Set<String> proxycom = new HashSet<>();
    for (String url : ProxyGetUtil.proxycom) {
      proxycom.addAll(getProxycomList(url));
    }
    return proxycom;
  }

  public static Set<String> getProxy() {
    Set<String> proxy = new HashSet<String>();
//    logger.info("will get");
    Set<String> pachong = new HashSet<>();
    try {
      pachong = getProxyListFromPaChongPage();
    } catch (Exception e) {
    }
    logger.info("get number pachong: " + pachong.size());
//    Set<String> freeproxy = new HashSet<>();
//    try {
//      freeproxy = getProxyListFromFreeProxyListPage();
//    } catch (Exception e) {
//      logger.warn(e.getMessage());
//      logger.warn(e);
//    }
//    logger.info("get number freeproxy: " + freeproxy.size());
//    Set<String> samair = getPureProxyList();
    Set<String> samair = new HashSet<>();
    try {
      samair = getPureProxy(samair_url[0]);
    } catch (Exception e) {
    }
    logger.info("get number samair: " + samair.size());
    Set<String> proxycom = new HashSet<>();
    try {
      proxycom = getProxycomList(ProxyGetUtil.proxycom[0]);
    } catch (Exception e) {
    }
    logger.info("get number proxycom: " + proxycom.size());

    proxy.addAll(pachong);
//    proxy.addAll(freeproxy);
    proxy.addAll(samair);
    proxy.addAll(proxycom);
    return proxy;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    long begin = System.currentTimeMillis();
    for (int i = 0; i < 10; i++)
      logger.info(getProxy().toString());
    logger.info("time: " + ((System.currentTimeMillis() - begin) / 1000));
  }

  public static Map<String, Integer> parseJS(String content) {
    Map<String, Integer> ans = new HashedMap<>();
    String[] tmps = content.split(";");
    for (String tmp : tmps) {
      tmp = tmp.trim();
//      logger.info(tmp);
      int i = tmp.indexOf(' ');
      int j = tmp.indexOf('=');
      if (i >= 0 && j >= 0 && i < j) {
        String param = tmp.substring(i + 1, j);
        String formula = tmp.substring(j + 1);
        int res = eval(formula, ans);
        ans.put(param, res);
      }
    }
    return ans;
  }

  private static int eval(String formula, Map<String, Integer> ans) {
//    if (formula.startsWith("(") && formula.endsWith(")")) {
//      return eval(formula.substring(1, formula.length() - 1), ans);
//    }
    int dengyu = formula.indexOf('^');
    if (dengyu > 0) {
      return eval(formula.substring(0, dengyu), ans) ^ eval(formula.substring(dengyu + 1), ans);
    } else if (formula.contains("+")) {
      String[] tmps = formula.split("\\+");
      int a = ans.containsKey(tmps[0]) ? ans.get(tmps[0]) : Integer.parseInt(tmps[0]);
      int b = ans.containsKey(tmps[1]) ? ans.get(tmps[1]) : Integer.parseInt(tmps[1]);
      return a + b;
    } else {
      return ans.containsKey(formula) ? ans.get(formula) : Integer.parseInt(formula);
    }
  }

  private static int eval2(String formula, Map<String, Integer> ans) {
    String[] tmps = formula.split("\\+");
    String t1 = tmps[0];
    String t2 = tmps[1];
    if (t1.startsWith("(") && t1.endsWith(")")) {
      t1 = t1.substring(1, t1.length() - 1);
    }
    String[] tmps2 = t1.split("\\^");
    int a = ans.containsKey(tmps2[0]) ? ans.get(tmps2[0]) : Integer.parseInt(tmps2[0]);
    int b = ans.containsKey(tmps2[1]) ? ans.get(tmps2[1]) : Integer.parseInt(tmps2[1]);
    int c = ans.containsValue(t2) ? ans.get(t2) : Integer.parseInt(t2);
    return (a ^ b) + c;
  }


}