package com.nether.wifip2pdemo.p2p;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class P2pThreadPool {

  private static Executor s = Executors.newCachedThreadPool();

  public static void work(Runnable runnable) {
    s.execute(runnable);
  }
}
