package com.neo.sk.arachnez.proxy;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/7/16
 * Time: 17:06
 */
public class DelayedObject implements Delayed {
  private String proxy;
  private long timeout;

  public String getProxy() {
    return proxy;
  }

  public DelayedObject(String proxy, long delayTime, TimeUnit unit) {
    this.proxy = proxy;
    long nanoTime = TimeUnit.NANOSECONDS.convert(delayTime, unit);
    this.timeout = nanoTime + System.nanoTime();
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(timeout - System.nanoTime(), TimeUnit.NANOSECONDS);
  }

  @Override
  public int compareTo(Delayed other) {
    if (other == this) {
      return 0;
    }
    DelayedObject t = (DelayedObject) other;
    long d = (getDelay(TimeUnit.NANOSECONDS) - t.getDelay(TimeUnit.NANOSECONDS));
    return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
  }
}
