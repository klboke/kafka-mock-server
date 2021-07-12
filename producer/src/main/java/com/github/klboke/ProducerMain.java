package com.github.klboke;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/7/9
 */
@SpringBootApplication
@RestController
public class ProducerMain {

  private static final Logger logger = LoggerFactory.getLogger(ProducerMain.class);
  private final KafkaTemplate<Object, Object> template;
  private static final AtomicInteger integer = new AtomicInteger();

  public static void main(String[] args) {
    SpringApplication.run(ProducerMain.class, args);
  }

  public ProducerMain(KafkaTemplate<Object, Object> template) {
    this.template = template;
  }
  @GetMapping("/send")
  public String sendFoo() {
    String message = "message:" + integer.incrementAndGet();
    this.template.send("topic001", message);
    return message;
  }

  @GetMapping("/send-message-by-time")
  public String sendMessageByTime() throws ExecutionException, InterruptedException {
    AtomicInteger counter = new AtomicInteger();
    LocalDateTime stopTime = LocalDateTime.now().plusMinutes(3);
    while (LocalDateTime.now().isBefore(stopTime)) {
      String message = "message:" + counter.incrementAndGet();
      //.get() 用于将异步发送变同步，异步发送本机受不了
      template.send("topic001", message).get();
      //同步发送还是太快了，这里阻塞小会儿
      TimeUnit.MILLISECONDS.sleep(100);
    }
    String result = "3 分钟总共发送了 " + counter.get() + " 条消息";
    logger.info(result);
    return result;
  }

  @KafkaListener(id = "webGroup", topics = "topic001")
  public void listen(String input) {
    logger.info("input value: {}", input);
  }

}
