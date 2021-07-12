package com.github.klboke;

import kafka.tools.MirrorMaker;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/7/9
 */
public class MirrorMarkerMain001 {

  /**
   * 开启 1 个消费线程的 kafka 集群同步服务
   * @param args 命令入参，可用来覆盖默认默认配置
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      args = ConfigProvider.getOneThreadConfig();
    }
    MirrorMaker.main(args);
  }
}
