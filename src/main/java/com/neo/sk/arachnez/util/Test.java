package com.neo.sk.arachnez.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by chenlingpeng on 2014/10/29.
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
//        System.out.println(System.currentTimeMillis());
//        long l1 = System.currentTimeMillis();
//        long l2 = System.nanoTime();
//        String html = "\"<p style=\"text-align: center;\"><img src=\"http://leiphone.qiniudn.com/uploads/new/article/600_600/201410/543ccfb35ff4c.jpg\" /></p>\u0002<p>相信大多数读者家中都有电视机的一席之地";
//        new Test().ddd();
//        System.out.println(html.replaceAll("\u0002",""));
//        System.out.println(System.nanoTime());
//        Thread.yield();
//        Thread.sleep(1000);
//
//        System.out.println(System.currentTimeMillis()-l1);
//        System.out.println(System.nanoTime()-l2);
    }
    public synchronized void ddd() throws InterruptedException {
        wait();
    }
}
