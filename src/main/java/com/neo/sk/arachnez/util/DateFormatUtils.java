package com.neo.sk.arachnez.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/9/7
 * Time: 14:13
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
public class DateFormatUtils {

  private static final Logger logger = LoggerFactory.getLogger(DateFormatUtils.class);


  public static final String yyyyMMddhhmmss = "yyyy-MM-dd HH:mm:ss";
  public static final String yyyyMMdd = "yyyy-MM-dd";
  public static final String yyyyMMdd2 = "yyyy/MM/dd";

  public static String formatTime(final long time, final String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat();
    sdf.applyPattern(pattern);
    return sdf.format(time);
  }

  public static String getTodayFormat(final String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat();
    sdf.applyPattern(pattern);
    return sdf.format(new Date());
  }

  public static long getTime(String date, String pattern) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat();
    sdf.applyPattern(pattern);
    return sdf.parse(date).getTime();
  }
}
