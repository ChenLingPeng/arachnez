package com.neo.sk.arachnez.jobs.tianya;

import com.neo.sk.arachnez.commons.SKURL;
import com.neo.sk.arachnez.framework.SKEngine;
import com.neo.sk.arachnez.thrift.ThriftClient;
import com.neo.sk.arachnez.util.DateFormatUtils;
import org.apache.thrift.TException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/9/7
 * Time: 13:57
 * <p/>
 * //                            _ooOoo_
 * //                           o8888888o
 * //                           88" . "88
 * //                           (| -_- |)
 * //                            O\ = /O
 * //                        ____/`---'\____
 * //                      .   ' \\| |// `.
 * //                       / \\||| : |||// \
 * //                     / _||||| -:- |||||- \
 * //                       | | \\\ - /// | |
 * //                     | \_| ''\---/'' | |
 * //                      \ .-\__ `-` ___/-. /
 * //                   ___`. .' /--.--\ `. . __
 * //                ."" '< `.___\_<|>_/___.' >'"".
 * //               | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * //                 \ \ `-. \_ __\ /__ _/ .-` / /
 * //         ======`-.____`-.___\_____/___.-`____.-'======
 * //                            `=---='
 * //
 * //         .............................................
 * //
 * //   █████▒█    ██  ▄████▄   ██ ▄█▀       ██████╗ ██╗   ██╗ ██████╗
 * // ▓██   ▒ ██  ▓██▒▒██▀ ▀█   ██▄█▒        ██╔══██╗██║   ██║██╔════╝
 * // ▒████ ░▓██  ▒██░▒▓█    ▄ ▓███▄░        ██████╔╝██║   ██║██║  ███╗
 * // ░▓█▒  ░▓▓█  ░██░▒▓▓▄ ▄██▒▓██ █▄        ██╔══██╗██║   ██║██║   ██║
 * // ░▒█░   ▒▒█████▓ ▒ ▓███▀ ░▒██▒ █▄       ██████╔╝╚██████╔╝╚██████╔╝
 * //  ▒ ░   ░▒▓▒ ▒ ▒ ░ ░▒ ▒  ░▒ ▒▒ ▓▒       ╚═════╝  ╚═════╝  ╚═════╝
 * //  ░     ░░▒░ ░ ░   ░  ▒   ░ ░▒ ▒░
 * //  ░ ░    ░░░ ░ ░ ░        ░ ░░ ░
 * //           ░     ░ ░      ░  ░
 * //
 */
public class PageParser {

  private static final Logger logger = LoggerFactory.getLogger(PageParser.class);

  private static final String reg01 = "post-(.*)-(.*)-(\\d*).shtml";
  private static final Pattern pattern01 = Pattern.compile(reg01);

  private static final String reg02 = "<span>时间：(.*) </span>";
  private static final Pattern pattern02 = Pattern.compile(reg02, Pattern.MULTILINE);

  private static final String reg03 = " (\\d*)楼 \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}";
  private static final Pattern pattern03 = Pattern.compile(reg03);

  private static final String reg04 = "<a href=\"art\\.jsp\\?item=.*id=(\\d*)&amp;p=(\\d*).*\">末页</a>";
  private static final Pattern pattern04 = Pattern.compile(reg04);

  private static final ThriftClient client = ThriftClient.getInstance();
  private static AtomicInteger count = new AtomicInteger(0);

  public static int parsePage(SKURL skurl, String jobName) {
    String content = skurl.getContent();
    if (skurl.getUrl().endsWith("&p=1")) {
      Matcher matcher04 = pattern04.matcher(content);
      int id = 0;
      if (matcher04.find()) {
        id = Integer.parseInt(matcher04.group(2));
        String pid = matcher04.group(1);
        // 两个id对不上
        if(!skurl.getUrl().contains("id="+pid+"&")){
//          logger.info("error, maybe a redirect page for: "+skurl.getUrl());
          return 0;
        }
//        logger.info("find pageid: "+id);
      } else {
//        logger.info(content);
//        logger.info("can't find pageid");
      }
      String base = skurl.getUrl().substring(0, skurl.getUrl().length() - 1);
      if(id>1) {
        logger.info("add comment url: " + base + id);
      }
      for (int i = 2; i <= id; i++) {
        SKEngine.addUrl(jobName, base + i);
      }
    }
    if (content.contains("该贴不存在")) {
//      logger.info(skurl.getUrl()+" 帖子不存在");
      return 0;
    }
    if (!content.contains("注册")) return 0;
    int i = content.indexOf("<body>");
    int j = content.lastIndexOf("</body>");
    if (i > 0 && j > 0 && i < j) {
      content = content.substring(i, j + 7).intern();
      try {
        client.addRecord(skurl.getUrl()+content, "/user/heritrix/ArachnezTianya3");
        int cnt = count.incrementAndGet();
        if (cnt % 100 == 0) {
          logger.info("submit record is: " + cnt);
        }
        return 1;
      } catch (Exception e) {
//        logger.warn(e.getMessage(), e);
      }
    }
    return 0;
  }

  public static int parsePage2(SKURL skurl, String jobName) {
    int tmpCnt = 0;
    Matcher matcher01 = pattern01.matcher(skurl.getUrl());
    String boardName = null;
    String postNumStr = null;
    logger.info("will parsePage");
    if (matcher01.find()) {
      boardName = matcher01.group(1);
      postNumStr = matcher01.group(2);
    } else {
      logger.info("no match01 will return");
      return 0;
    }
    Document doc = Jsoup.parse(skurl.getContent());
    Elements eles = doc.select(".s_title");
    if (eles.size() != 1) {
      logger.info("no s_title, will return");
      return 0;
    }
    String title = eles.first().text();
    TianyaBean bean = null;
    try {
      bean = new TianyaBean(boardName, title, Long.parseLong(postNumStr));
    } catch (NumberFormatException e) {
      logger.warn(e.getMessage(), e);
    }

    if (skurl.getUrl().endsWith("-1.shtml")) {
      // 第一页，包含主贴
//      logger.info("main page parse");
      Element ele = doc.getElementById("post_head");
      String uid = null;
      long ctime = 0l;
      if (ele != null) {
        String author = ele.select(".atl-info").select("a").attr("uid");
        uid = author;
        try {
          ctime = Long.parseLong(ele.select(".atl-menu").attr("js_posttime"));
        } catch (Exception e) {
          logger.warn(e.getMessage(), e);
        }
      }
//      logger.info("main page parse2");
      eles = doc.select(".atl-main").select(".atl-item");
      for (Element item : eles) {
        logger.info("next item");
        String content = item.select(".bbs-content").html();
        bean.setContent(content);
        if (item.hasAttr("id")) {
          long floor = 0l;
          try {
            floor = Long.parseLong(item.attr("id"));
          } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            continue;
          }
          bean.setFloor(floor);

          try {
            String author = item.select(".atl-head").select(".atl-info").select("a").attr("uid");
            bean.setAuthor(author);
            long time = DateFormatUtils.getTime(item.attr("js_resTime"), "yyyy-MM-dd HH:mm:ss");
            bean.setCtime(time);
          } catch (ParseException e) {
            logger.warn(e.getMessage(), e);
            continue;
          } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            continue;
          }
//          logger.info("main page parse3");
          Matcher match03 = pattern03.matcher(content);
//          logger.info("main page parse4");
          if (match03.find()) {
            long reid = 0;
            try {
              reid = Long.parseLong(match03.group(match03.groupCount()));
            } catch (NumberFormatException e) {
              logger.warn(e.getMessage(), e);
            } catch (Exception e) {
              logger.warn(e.getMessage(), e);
            }
            bean.setTofloor(reid);
          } else {
            bean.setTofloor(0l);
          }
//          logger.info("main page parse5");
        } else {
          bean.setAuthor(uid);
          bean.setCtime(ctime);
          bean.setFloor(0);
          bean.setTofloor(0);
        }

        logger.info("【will add thrift】");
//        synchronized (lock) {
        try {
          client.addRecord(bean.toString(), "/user/heritrix/ArachnezTianya");
          int cnt = count.incrementAndGet();
          tmpCnt++;
          logger.info("submit record is: " + cnt);
        } catch (TException e) {
          logger.warn(e.getMessage(), e);
        }
//        }
      }
//      logger.info("main page parse6");
      eles = doc.select(".atl-pages");
      if (eles.size() == 1) {
        eles = eles.first().select("a");
        long pagenum = -1;
        for (Element e : eles) {
          try {
            long tmppage = Long.parseLong(e.text());
            pagenum = Math.max(pagenum, tmppage);
          } catch (Exception e1) {
            logger.warn(e1.getMessage(), e1);
          }
        }
//        logger.info("main page parse7");
        String baseUrl = skurl.getUrl().substring(0, skurl.getUrl().length() - 7).intern();
        for (int i = 2; i <= pagenum; i++) {
          SKEngine.addUrl(jobName, baseUrl + i + ".shtml");
        }
      }
    } else {
      // 回复页
//      logger.info("main page parse8");
      Elements items = doc.select(".atl-main").select(".atl-item");
      for (Element item : items) {
        logger.info("next item2");
        String content = item.select(".bbs-content").html();
        bean.setContent(content);
        long floor = 0l;
        try {
          floor = Long.parseLong(item.attr("id"));
        } catch (Exception e) {
          logger.warn(e.getMessage(), e);
          continue;
        }
        bean.setFloor(floor);
        try {
          String author = item.select(".atl-head").select(".atl-info").select("a").attr("uid");
          bean.setAuthor(author);
          long time = DateFormatUtils.getTime(item.attr("js_resTime"), "yyyy-MM-dd HH:mm:ss");
          bean.setCtime(time);
        } catch (ParseException e) {
          logger.warn(e.getMessage(), e);
          continue;
        } catch (Exception e) {
          logger.warn(e.getMessage(), e);
          continue;
        }
//        logger.info("main page parse9");
        Matcher match03 = pattern03.matcher(content);
        if (match03.find()) {
          try {
            long reid = Long.parseLong(match03.group(match03.groupCount()));
            bean.setTofloor(reid);
          } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            bean.setTofloor(0l);
          }
        } else {
          bean.setTofloor(0l);
        }
        logger.info("【will add thrift】");
//        synchronized (lock) {
        try {
          client.addRecord(bean.toString(), "/user/heritrix/ArachnezTianya");
          int cnt = count.incrementAndGet();
          tmpCnt++;
          logger.info("submit record is: " + cnt);
        } catch (TException e) {
          logger.warn(e.getMessage(), e);
        }
      }
//      }
    }
    return tmpCnt;
  }
}
