package com.neo.sk.arachnez.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * User: May
 * Date: 2014-12-24
 * Time: 17:19
 */
public class CaicaiMysql {

    private static Connection conn =  getConnection("caicai", "caicai1234", "10.1.1.37", "3306", "CaiCai");

    public static Connection getConn() {
        return conn;
    }

    public static Connection getConnection(String user, String pwd, String host, String port, String database) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?" +
                    "useUnicode=true&characterEncoding=utf-8";   //10.1.1.19
            conn = DriverManager.getConnection(url, user, pwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void addRecords(String record) {
        try {
            Connection conn = getConn();
            if (conn != null) {
                Statement stmt = conn.createStatement();
                StringBuilder sql =
                        new StringBuilder(
                                "insert into lottery(" +
                                        "lot_id," +
                                        "type," +
                                        "number," +
                                        "issue)" +
                                        " values('");
                String[] infos = record.split("\u0001");
                for (String info : infos) {
                    sql.append(info).append("','");
                }
                sql.deleteCharAt(sql.length() - 2);
                sql.deleteCharAt(sql.length() -1);
                sql.append(");");
                System.out.println(sql.toString());
                stmt.execute(sql.toString());
//                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public  static void addPredict(String predict){
        try{
            Connection conn = getConn();
            if(conn!=null) {
                Statement stmt = conn.createStatement();
                StringBuilder sql =
                        new StringBuilder(
                                "insert into predict(" +
                                        "lot_id," +
                                        "title," +
                                        "link,"+
                                        "time)"+
                                        "values('"
                        );
                String [] infos = predict.split("\u0001");
                for(String info: infos){
                    sql.append(info).append("','");
                }
                sql.deleteCharAt(sql.length() - 2);
                sql.deleteCharAt(sql.length() -1);
                sql.append(");");
                System.out.println(sql.toString());
                stmt.execute(sql.toString());
//                conn.close();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void addGame(String record) {
        try {
            Connection conn = getConn();
            if (conn != null) {
                Statement stmt = conn.createStatement();
                StringBuilder sql =
                        new StringBuilder(
                                "insert into game(" +
                                        "lot_id," +
                                        "issue," +
                                        "event," +
                                        "event_time," +
                                        "visit_team," +
                                        "home_team," +
                                        "score," +
                                        "game_order)" +
                                        " values('");
                String[] infos = record.split("\u0001");
                for (String info : infos) {
                    sql.append(info).append("','");
                }
                sql.deleteCharAt(sql.length() - 2);
                sql.deleteCharAt(sql.length() -1);
                sql.append(");");
                System.out.println(sql.toString());
                stmt.execute(sql.toString());
//                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
