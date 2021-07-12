package com.github.klboke;

import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/7/9
 */
@SpringBootApplication
@RestController
public class ConsumerMain {

  private static final Logger logger = LoggerFactory.getLogger(ConsumerMain.class);
  private static final AtomicInteger counter = new AtomicInteger();

  public static void main(String[] args) {
    SpringApplication.run(ConsumerMain.class, args);
  }

  @KafkaListener(id = "ConsumerMain", topics = "topic001")
  public void listen(String input) {
    logger.info("总计:{},{}", counter.incrementAndGet(), input);
  }

}
