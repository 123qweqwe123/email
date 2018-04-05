## 邮件发送服务

### 介绍
目前系统发送邮件频率太快，会出现 `554 Reject by behaviour spamANTISPAM_BAT[01201311R4368S5030111229, e01e07417]: too frequently sending` 异常。解决方案，将要发送的邮件放到 rabbitmq 的队列中，定时去消费。如果因为其它原因依然发送失败，会自动将失败原因发送到指定的邮箱(可以配置)。

### 使用

参见测试用例 `com.bdcor.service.SenderTest`

**通过 restful API 的方式**

```
# 通过类似httpclient工具post请求调用如下url
http://ip:port/service/mail/send
# post参数如下
# 邮件标题
subject 
# 邮件内容
content
# 收件人(可以为数组)
to
# 抄送(可以为数组)
copyTo
# 两种方式添加附件，附件名+附件路径 或者附件
# 附件名(可以为数组)
fileNames
# 附件路径(可以为数组),顺序和附件名保持一致
filePaths
# 附件(可以为数组，附件名默认为file.getName())
files
```
**直接通过 rabbitmq java 客户端往队列添加消息**

``` java
    private final static String QUEUE_NAME = "mail-queue";

    @Test
    public void testSendMail() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        MailVO mail = new MailVO();
        mail.setContent("您好：...");
        mail.setSubject("邮件主题");
        List<String> to = Lists.newArrayList();
        to.add("11111111@qq.com");
        mail.setTo(to);
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().contentType(MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT)
            .build();
        channel.basicPublish("mail-exchange", QUEUE_NAME, properties, SerializationUtils.serialize(mail));
        System.out.println(" [x] Sent '...'");

        channel.close();
        connection.close();
    }
    
public class MailVO implements Serializable {

    private List<String> to;
    private List<String> copyTo;
    private String subject;
    private String content;
    private List<String> fileNames;
    private List<String> filePaths;
    private Vector files = new Vector();
    private LinkedHashMap<String, String> filesWithName = Maps.newLinkedHashMap();
    private String errMsg;
```

### 配置

### 服务器配置

**测试服务器**
服务地址：  
http://10.24.10.225:8080/mail-service/service/mail

**生产服务器**


## rabbitmq 消息队列

详细安装教程见 http://10.24.10.225:8080/docs/RabbitMQ-Install-And-Settings.html

### 介绍

### 使用

### 配置

**测试服务器**
ip/port 10.24.10.225:5672
web 端 http://10.24.10.225:15672 guest/guest

**生产服务器**


## 分布式文件存储服务

### 介绍

fastdfs 是一个开源轻量级分布式文件系统，特别适合以中小文件（4KB<file_size<500MB)为载体的在线服务。我看了这个系统虽然没有一个正式的官网(貌似网站挂了)，但是作者还在一直持续维护，而且软件架构本身也很清晰，对于软件的讨论也主要集中在[ChinaUnix](http://bbs.chinaunix.net/forum-240-1.html)。
详细安装教程见 http://10.24.10.225:8080/docs/FastDFS-Install-And-Settings.html

### 使用

客户端（我自己编译，已上传到 nexus）,添加 maven 依赖。

```
<dependency>
    <groupId>org.csource</groupId>
    <artifactId>fastdfs-client-java</artifactId>
    <version>1.27</version>        
</dependency>
```
工具类 `FastDFUtils`，封装了上传下载和删除操作，具体 api 请下载 `fastdfs-client-java-sources.jar`

``` java
// FastDFSUtils 封装了文件上传下载和删除操作
// 可以通过web端下载上传的文件 
// http://10.24.10.225/group1/M00/00/00/ChgK4VkSuHOAb2ctAADvKofo4S0504_big.jpg
String fileId = FastDFSUtils.uploadFile("10.24.10.225:22122","test.jpg");
FastDFSUtils.downloadFile(...)
```
### 配置

**测试服务器**
ip/port 10.24.10.225:22122




