package com.bdcor.service;

import com.bdcor.services.mail.vo.MailVO;
import com.google.common.collect.Lists;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.csource.common.MyException;
import org.csource.fastdfs.FastDFSUtils;
import org.junit.Test;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.utils.SerializationUtils;

import javax.activation.FileTypeMap;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Description:
 * Author: huangrupeng
 * Create: 17/5/8 上午9:35
 */
public class SenderTest {

    private final static String QUEUE_NAME = "mail-queue";

    @Test
    public void testSendMail2queue() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        MailVO mail = new MailVO();
        mail.setContent("您好：111111");
        mail.setSubject("222sdfsdf司法所地方水电费水电费水电费");
        List<String> to = Lists.newArrayList();
        to.add("532093012@qq.com");
        mail.setTo(to);
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().contentType(MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT)
            .build();
        channel.basicPublish("mail-exchange", QUEUE_NAME, properties, SerializationUtils.serialize(mail));
        System.out.println(" [x] Sent '...'");

        channel.close();
        connection.close();
    }

    @Test
    public void testSendFailMail2queue() throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Map<String, Object> args = new HashMap<String, Object>();
        //args.put("x-max-length", 100000);
        args.put("x-delayed-type", "direct");
        channel.exchangeDeclare("mail-fail-exchange", "x-delayed-message", true, false, args);

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = "Hello World!";
        MailVO mail = new MailVO();
        mail.setContent("您好：这是要发送的邮件222");
        mail.setSubject("邮件标题11");
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("x-delay", 10000);
        headers.put("x-redelivered-count", 3);
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().contentType(MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT)
            .headers(headers).build();
        channel.basicPublish("mail-exchange", QUEUE_NAME, properties, SerializationUtils.serialize(mail));
        System.out.println(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }

    @Test
    public void testDataHandler() {
        System.out.println(FileTypeMap.getDefaultFileTypeMap().getContentType("/Users/huangrupeng/Downloads/高危三期服务器列表.xlsx"));    //image/jpeg
    }

    /**
     * 测试带附件的邮件发送
     *
     * @throws IOException
     */
    @Test
    public void testSendMailWithRestfulAPI() throws IOException, MyException {
        String fileId1 = FastDFSUtils.uploadFile("10.24.10.12:22122", "/Users/huangrupeng/Downloads/测试附件1.docx");
        String fileId2 = FastDFSUtils.uploadFile("10.24.10.12:22122", "/Users/huangrupeng/Downloads/测试附件2.xlsx");
        String fileId3 = FastDFSUtils.uploadFile("10.24.10.12:22122", "/Users/huangrupeng/Downloads/测试附件3.png");

        Request.Post("http://10.24.10.17:8080/mail-service/service/mail/send")
            .bodyForm(Form.form().
                add("to", "huang.rp@jnyl-tech.com").
                add("to", "532093012@qq.com").
                add("subject", "《D0064-5001-20170511-01可能是电话号码，是否拨号?-高危3期-生物样本转运申请》").
                add("content", "测试邮件系统（生产环境）").
                add("fileNames", "测试附件1.docx").
                add("filePaths", fileId1).
                add("fileNames", "测试附件2.xlsx").
                add("filePaths", fileId2).
                add("fileNames", "测试附件3.png").
                add("filePaths", fileId3).
                build(), Charset.forName("utf-8"))
            .execute().returnContent();
    }

}