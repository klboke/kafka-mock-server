# kafka-mock-server
用于搭建测试 kafka 集群，测试 kafka 消息发送、消费，kafka 消息集群同步的项目。有如下模块。

## mock-server
mock-server 用来创建 kafka 集群，进入项目的测试模块下，有如下创建 kafka 服务的启动器：

- KafkaMockServer9097：创建一个端口号为 9097 的单 broker 的 kafka 服务，zookeeper 端口号随机
- KafkaMockServer9098：创建一个端口号为 9098 的单 broker 的 kafka 服务，zookeeper 端口号随机
- MultipleBroker001：创建端口号为 9090、9091、9092 的多 broker 的 kafka 服务，zookeeper 端口号 59551
- MultipleBroker002：创建端口号为 9095、9096、9097 的多 broker 的 kafka 服务，zookeeper 端口号 59552

## producer
producer 用来发送 kafka 消息，并监听自己发送的消息，消息发送做了计数统计，用于和同步 kafka 集群接收的的消息数据做对比
```java
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
  public String send() {
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
```
producer 是一个 spring-boot 的 web 应用，默认启动 8089 端口，项目启动成功后，访问
- http://127.0.0.1:8089/send : send() 方法发送单条消息，用于观察验证 topic 是否正常发送消费。
- http://127.0.0.1:8089/send-message-by-time : sendMessageByTime() 方法持续同步发送消息三分钟，
用于验证 mirror-maker 服务从(MirrorMarkerMain001单一消费线程服务) 切换到 MirrorMarkerMain002(多线程消费)的消息准确性，准确性包括
消息是否丢失、消息是否重复

## consumer
consumer 用来消费 MirrorMaker 同步到 kafka 集群的消息，消息到的消息也做了计数，用于和发出的消息数据做对比
```java
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
```
每做一次消息同步测试，都需要重启 ConsumerMain 服务，用于重置消息计数器，只有当计数器相等时，才认为消息同步成功无丢失无冗余

## mirror-maker
mirror-maker 模块用于 kafka 消息集群同步，分别创建单线程的消费实例和多线程消费实例，观察消费者线程重新平衡的过程。
```java
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
```
## kafka-manager
项目地址 ：https://github.com/yahoo/CMAK

kafka-manager 是雅虎开源的管理 kafka 集群的项目，kafka-manager 本身也是集群架构，需要依赖 zookeeper。项目拉下来后，先修改 conf/application.conf 中的
zookeeper 配置，然后根目录下执行 `./sbt start` 启动项目。默认 web 端口是 9000，启动成功后，访问 http://127.0.0.1:9000 

在 web 页面上添加需要管理的 kafka 集群时，只需要配置 kafka 集群的 zookeeper 地址即可。

# 测试 MirrorMarker 启停，组件工作是否正常
操作步骤如下，使用 kafka-manager 观察：
- 1、分别启动 MultipleBroker001、MultipleBroker002、MirrorMarkerMain001、producer、consumer、kafka-manager 等服务
- 2、触发 producer 的 /send 接口，检查服务是否正常工作
- 3、触发 producer 的 /send-message-by-time 接口，观察消息发送、消费情况，MirrorMarker 同步情况（消息总共会同步发送三分钟，期间完成如下操作）
- 4、启动 MirrorMarkerMain002 服务，观察同步消费组的 Consumer Instance Owner 变化
- 5、停止 MirrorMarkerMain001 服务，观察同步消费组的 Consumer Offset 状态
- 6、测试结束，对比发送的消息总数和同步集群消费到的总数，观察 LogSize 和 Consumer Offset 是否一致
