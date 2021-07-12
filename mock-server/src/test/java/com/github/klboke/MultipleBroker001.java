package com.github.klboke;

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/7/12
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = KafkaMockServer9097.class)
@EmbeddedKafka(
    partitions = 12,
    count = 3,
    zookeeperPort = 59551,
    ports = {9090, 9091, 9092}
)
public class MultipleBroker001 {

  @Test
  public void start() throws IOException {
    System.in.read();
  }

}
