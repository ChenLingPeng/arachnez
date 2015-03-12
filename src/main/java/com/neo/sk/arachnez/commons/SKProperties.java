package com.neo.sk.arachnez.commons;

import com.neo.sk.arachnez.client.SKClientJob;
import com.neo.sk.arachnez.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: jiameng
 * Date: 2014/8/26
 * Time: 12:53
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
public class SKProperties {
  private static final Logger logger = LoggerFactory.getLogger(SKProperties.class);
  private String filePath = null;
  private String propertiesPath = null;
  private Properties prop;
  private SKClientJob SKCJob;
  private int delaySeconds = 600;
  private long sleepTime = 0l;
  private int threadNum = 1;
  private String jobName = null;
  private String jarName = null;
  private boolean isProxy = true;
  private String classPath = null;
  private String charset = null;
  private boolean isUsable = true;

  public SKProperties(String filePath) {
    this.filePath = filePath;
    this.propertiesPath = filePath + "/skspider.properties";
    getProperties(propertiesPath);
  }

  private void getProperties(String propertiesPath) {
    prop = PropertiesUtil.loadFile(propertiesPath);
    if (prop.get("delaySeconds") != null) {
      String[] times = ((String) prop.get("delaySeconds")).split(":");
      if (times.length == 3) {
        int hour = Integer.parseInt(times[0]);
        int minute = Integer.parseInt(times[1]);
        int seconds = Integer.parseInt(times[2]);
        delaySeconds = (hour * 60 * 60 + minute * 60 + seconds);
      }
    } else delaySeconds = 0;

    if (prop.get("jobName") != null)
      jobName = (String) prop.get("jobName");
    else {isUsable = false;return;}

    if (prop.get("jarName") != null)
      jarName = (String) prop.get("jarName");
    else {isUsable = false;return;}

    if (prop.get("charset") != null)
      charset = (String) prop.get("charset");
    else isUsable = false;

    if (prop.get("threadNum") != null)
      threadNum = Integer.parseInt((String)prop.get("threadNum"));

    if (prop.get("sleepTime") != null)
      sleepTime = Long.parseLong((String)prop.get("sleepTime"));
    else isUsable = false;

    if (prop.get("isProxy") != null)
      isProxy = ((String) prop.get("isProxy")).equals("true");

    if (prop.get("classPath") != null) {
      try {
        File path = new File(filePath, getJarName());
        if (!path.exists()) {
          isUsable = false;
          return;
        }
        classPath = (String) prop.get("classPath");
        String jar_path = "file:" + path.getAbsolutePath();
        ClassLoader cl =
            new URLClassLoader(
                new URL[]{new URL(jar_path)},
                Thread.currentThread().getContextClassLoader());
        Class<?> c = cl.loadClass(classPath); //从加载器中加载Class
        SKCJob = (SKClientJob) c.newInstance();
      } catch (Exception e) {
        logger.warn(e.getMessage(), e);
      }
    } else isUsable = false;

  }

  public int getDelaySeconds() {
    return delaySeconds;
  }

  public String getJobName() {
    return jobName;
  }

  public String getJarName() {
    return jarName;
  }

  public String getCharset() {
    return charset;
  }

  public int getThreadNum() {
    return threadNum;
  }

  public long getSleepTime() {
    return sleepTime;
  }

  public boolean isProxy() {
    return isProxy;
  }

  public SKClientJob getSKCJob() {
    return SKCJob;
  }

  public String getClassPath() {
    return classPath;
  }

  public boolean isUsable() {
    return isUsable;
  }
}
