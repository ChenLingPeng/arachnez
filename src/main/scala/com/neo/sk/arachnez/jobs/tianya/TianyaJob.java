package com.neo.sk.arachnez.jobs.tianya;

import com.neo.sk.arachnez.client.SKClientExtractor;
import com.neo.sk.arachnez.client.SKClientJob;
import com.neo.sk.arachnez.commons.SKContext;
import com.neo.sk.arachnez.commons.SKStatistic;
import com.neo.sk.arachnez.commons.SKURL;
import com.neo.sk.arachnez.framework.SKEngine;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/9/7
 * Time: 11:41
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
public class TianyaJob extends SKClientJob {
  private static final Logger logger = LoggerFactory.getLogger(TianyaJob.class);
  private static final Object lock = new Object();
  private static final String reg01 = "&id=(\\d*)&";
  private static final Pattern pattern01 = Pattern.compile(reg01);

  private static final String reg02 = "list\\.jsp\\?item=(.*)";
  private static final Pattern pattern02 = Pattern.compile(reg02);

  private Map<String, Object> maxBoardIdMap;
  private SKStatistic skStatistic;
  private String maxFileName;
  private SKContext skContext;

  public TianyaJob() {
    this.maxFileName = "tianya/maxfile.txt";
    this.skContext = new SKContext(maxFileName);
    this.skStatistic = new SKStatistic("tianya");
  }

  @Override
  public SKClientExtractor getExtractor() {
    return new SKClientExtractor() {
      @Override
      public void process(SKURL skurl, String jobName) {
//        logger.info("process "+skurl.getUrl());
        if (skurl.isseed() && skurl.getUrl().contains("list.jsp")) {
//          synchronized (lock) {
//            if (TianyaJob.this.skStatistic == null || TianyaJob.this.maxFileName == null) {
//              TianyaJob.this.skStatistic = new SKStatistic(jobName);
//              TianyaJob.this.maxFileName = jobName+"/maxfile_test.txt";
//              TianyaJob.this.skContext = new SKContext(TianyaJob.this.maxFileName);
//              TianyaJob.this.maxBoardIdMap = TianyaJob.this.skContext.GetOldMaxArticleList();
//            }
//          }
          logger.info("seed process: " + skurl.getUrl());
          logger.info("seed page: " + skurl.getContent());
          Matcher matcher02 = pattern02.matcher(skurl.getUrl());
          String boardName = null;
          if (matcher02.find()) {
            boardName = matcher02.group(1);
          }

          Document doc = Jsoup.parse(skurl.getContent());
          Elements eles = doc.select("span.icon-faceblue");
          if (eles.size() > 0) {
            logger.info("seed parse" + eles.size());
            Element ele = eles.first().parent();
            String url = ele.select("a").attr("href");
            Matcher matcher01 = pattern01.matcher(url);
            if (matcher01.find()) {
              String maxidStr = matcher01.group(1);
              long maxid = Long.parseLong(maxidStr);
              maxid = (long) (maxid * 0.99);
              long oldMaxid = MapUtils.getLong(maxBoardIdMap, boardName, 1l);
              maxid = Math.max(maxid, oldMaxid);
              logger.info("will add "+(maxid - oldMaxid) +" urls");
              for (long i = oldMaxid; i < maxid; i++) {
                String newUrl = "http://3g.tianya.cn/bbs/art.jsp?item=" + boardName + "&id=" + i + "&p=1";
                SKEngine.addUrl(jobName, newUrl);
              }
              maxBoardIdMap.put(boardName, maxid);
              skContext.saveMaxList(maxBoardIdMap);
            }
          } else {
            logger.info("error parse");
          }
        } else if (skurl.getUrl().contains("art.jsp")) {
//          logger.info("post process: "+skurl.getUrl());
          try {
//            logger.info("will parse");
            int size = PageParser.parsePage(skurl, jobName);
//            logger.info("parse finish");
            if (size > 0)
              skStatistic.addItems(size, size);
//            logger.info("add item finish");
          } catch (Exception e) {
            logger.warn(e.getMessage(), e);
          }
        }
//        logger.info("end process seed url"+skurl.getUrl());
      }
    };
  }


  @Override
  public void flush(String filePath) {
    skStatistic.flushData(filePath);
  }

  @Override
  public void initContext() {
//    this.maxBoardIdMap = new ConcurrentHashMap<>();
    this.maxBoardIdMap = skContext.GetOldMaxArticleList();
//    this.maxBoardIdMap = skContext.
  }

  public static void main(String[] args) throws IOException {
    String s = FileUtils.readFileToString(new File("C:\\Users\\chenlingpeng\\Desktop\\a.txt"));
    Document doc = Jsoup.parse(s);
    Elements eles = doc.select("span.icon-faceblue");
    if (eles.size() > 0) {
      logger.info("seed parse" + eles.size());
      Element ele = eles.first().parent();
      String url = ele.select("a").attr("href");
      Matcher matcher01 = pattern01.matcher(url);
      if (matcher01.find()) {
        String maxidStr = matcher01.group(1);
        long maxid = Long.parseLong(maxidStr);
        maxid = (long) (maxid * 0.99);
        System.out.println(maxid);
      }
    } else {
      logger.info("error parse");
    }
  }
}