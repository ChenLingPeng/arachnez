package com.neo.sk.arachnez.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

/**
 * User: jiameng
 * Date: 2014/9/18
 * Time: 14:53
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
public class DbUtil {
  private static DataSource pool = new ComboPooledDataSource();
  private static final Logger logger = Logger.getLogger(DbUtil.class);

  public static void init(String configFile){
//    Properties = PropertiesUtil.loadFile(configFile);
    Properties properties = new Properties();
    try {
      properties.load(DbUtil.class.getClassLoader().getResourceAsStream(configFile));
    } catch (IOException e) {
      logger.warn(e.getMessage());
      logger.warn(e);
    }
    String driverClass = properties.getProperty("c3p0.driverClass","com.mysql.jdbc.Driver");
    String jdbcUrl = properties.getProperty("c3p0.jdbcUrl","jdbc:mysql://10.1.1.37:3306/arachnez?characterEncoding=utf-8");
    String user = properties.getProperty("c3p0.user","arachnez");
    String password = properties.getProperty("c3p0.password","123456");
    int initialPoolSize = Integer.parseInt(properties.getProperty("c3p0.initialPoolSize", "3"));
    int minPoolSize = Integer.parseInt(properties.getProperty("c3p0.minPoolSize","3"));
    int acquireIncrement = Integer.parseInt(properties.getProperty("c3p0.acquireIncrement","3"));
    int maxPoolSize = Integer.parseInt(properties.getProperty("c3p0.maxPoolSize","15"));
    int maxIdleTime = Integer.parseInt(properties.getProperty("c3p0.maxIdleTime","100"));
    int acquireRetryAttempts = Integer.parseInt(properties.getProperty("c3p0.acquireRetryAttempts","30"));
    int acquireRetryDelay = Integer.parseInt(properties.getProperty("c3p0.acquireRetryDelay","1000"));
    ComboPooledDataSource pool = (ComboPooledDataSource) DbUtil.pool;
    try {
      pool.setDriverClass(driverClass);
      pool.setJdbcUrl(jdbcUrl);
      pool.setUser(user);
      pool.setPassword(password);
      pool.setInitialPoolSize(initialPoolSize);
      pool.setMinPoolSize(minPoolSize);
      pool.setAcquireIncrement(acquireIncrement);
      pool.setMaxPoolSize(maxPoolSize);
      pool.setMaxIdleTime(maxIdleTime);
      pool.setAcquireRetryAttempts(acquireRetryAttempts);
      pool.setAcquireRetryDelay(acquireRetryDelay);
    } catch (PropertyVetoException e) {
      logger.error("PropertyVetoException happened while init DBOperate", e);
    }

  }

  public static <T>  T select(String sql, ResultSetHandler<T> handler, Object... params) throws SQLException {
    QueryRunner qr = new QueryRunner(pool);
    return qr.query(sql,handler,params);

  }

  public static List<Object[]> selectArrayList(String sql,  Object... params) throws SQLException {
    QueryRunner qr = new QueryRunner(pool);
    return qr.query(sql,new ArrayListHandler(),params);
  }

  public static boolean update(String sql, Object... params) throws SQLException{
    QueryRunner qr = new QueryRunner(pool);
    return (qr.update(sql,params)==1);
  }

  /**
   * 对有自增id数据表的插入，成功返回自增id，失败返回-1
   * @param sql
   * @param params
   * @return
   * @throws java.sql.SQLException
   */
  public static Long insert(String sql, Object... params) throws SQLException{
    QueryRunner qr = new QueryRunner(pool);
    boolean result = (qr.update(sql,params)==1);

    if(result == false) {
      return -1L;
    } else {
      return  (Long)qr.query(pool.getConnection(), "SELECT LAST_INSERT_ID()", new ScalarHandler(1));
    }

  }
}
