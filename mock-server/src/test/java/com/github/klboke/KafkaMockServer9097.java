package com.github.klboke;

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/7/9
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = KafkaMockServer9097.class)
@EmbeddedKafka(
    partitions = 12,
    zookeeperPort = 59661,
    brokerProperties = {
        "listeners=PLAINTEXT://172.26.202.128:9097",
        "port=9097"
    })
public class KafkaMockServer9097 {


  @Test
  public void start() throws IOException {
    System.in.read();
  }

}
