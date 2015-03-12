package com.neo.sk.arachnez.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/9/29
 * Time: 11:39
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
public class URLUtils {
  private static final Logger logger = LoggerFactory.getLogger(URLUtils.class);
  private static final String URL_PATH = System.getProperty("user.dir") + "/seed/";
  private static final File base = new File(URL_PATH);

  static {
    if(!base.exists()){
      base.mkdir();
    }
  }

  public static List<String> loadJobURL(String jobName){
    File[] files = base.listFiles(new SeedFileFilter(jobName));
    List<String> seeds = new ArrayList<String>();
    if(files!=null && files.length>0){
      File f = files[0];
      logger.info("loading seed file: "+f.getName());
      try {
        seeds = FileUtils.readLines(f);
        f.delete();
        logger.info("loading seed file: "+f.getName()+" finish");
      } catch (IOException e) {
        logger.error(e.getMessage(),e);
      }
    }
    return seeds;
  }

  public static void saveJobURL(List<String> seed, String jobName){
    try {
      FileUtils.writeLines(new File(URL_PATH +jobName+"-"+System.currentTimeMillis()), seed);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static void clearJobURL(String jobName){
    File[] files = base.listFiles(new SeedFileFilter(jobName));
    for(File f:files){
      f.delete();
    }
  }

  public static void clearAllURL(){
    File[] files = base.listFiles();
    for(File f:files){
      f.delete();
    }
  }

  private static class SeedFileFilter implements FileFilter{
    private String jobName;

    public SeedFileFilter(String jobName){
      this.jobName = jobName;
    }

    @Override
    public boolean accept(File pathname) {
      if(pathname.isFile() && pathname.getName().startsWith(this.jobName+"-")){
        return true;
      }
      return false;
    }
  }
}
