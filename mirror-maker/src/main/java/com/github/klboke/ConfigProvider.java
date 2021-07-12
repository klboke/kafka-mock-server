package com.github.klboke;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/7/9
 */
public class ConfigProvider {

  private ConfigProvider(){}

  private static final String DEFAULT_CONFIG = "--num.streams=12 --whitelist=.* --producer.config=/Users/kl/githubnamespace/kafka-mock-server/mirror-maker/src/main/resources/producer.properties --consumer.config=/Users/kl/githubnamespace/kafka-mock-server/mirror-maker/src/main/resources/consumer.properties";

  public static String[] getConfig(){
    return DEFAULT_CONFIG.split(" ");
  }

  public static String[] getOneThreadConfig(){
    String config = DEFAULT_CONFIG.replace("--num.streams=12","--num.streams=1");
    return config.split(" ");
  }
}
