package com.bdcor.services.mail.task;

import com.bdcor.services.mail.exception.ServiceException;
import com.bdcor.services.mail.util.Mail;
import com.bdcor.services.mail.vo.MailVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;

/**
 * Description:
 * <pre>
 *     1.发送邮件，按照3s的间隔从队列里面获取邮件信息来发送
 *     2.如果发送失败，将失败信息放到延时队列里面
 *     3.每隔3s从延时队列里面获取发送失败信息发送到指定的邮箱进行人工干预
 * </pre>
 * Author: huangrupeng
 * Create: 17/5/8 上午9:14
 */
public class MailTask {
    Logger logger = LoggerFactory.getLogger(MailTask.class);

    /**
     * 发送频率，默认10s
     */
    private static final long MAIL_SEND_RATE = 10 * 1000;
    /**
     * 发送错误邮件延迟时间，默认60s
     */
    private static final long MAIL_DELAY = 60 * 1000;
    /**
     * 重发尝试次数
     */
    public static final int RETRY_TIMES = 10;
    /**
     * 重发邮件延迟时间
     */
    private static final long MAIL_RETRY_DELAY = 60 * 1000;

    private final RabbitTemplate rabbitTemplate;

    public MailTask(RabbitTemplate rabbitTemplate) {
        rabbitTemplate.setMessageConverter(new SerializerMessageConverter());
        // 设置延时队列，消息在指定MAIL_DELAY才能被消费
        rabbitTemplate.setBeforePublishPostProcessors((MessagePostProcessor) message -> {
            MessageProperties prop = message.getMessageProperties();
            // 设置延时时间
            prop.setHeader("x-delay", MAIL_DELAY);
            return message;
        });
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${mail.send.queue}")
    String mailSend;
    @Value("${mail.retry.queue}")
    String mailRetry;
    @Value("${mail.retry.exchange}")
    String mailRetryExchange;
    @Value("${mail.fail.queue}")
    String mailFail;
    @Value("${mail.fail.exchange}")
    String mailFailExchange;


    /**
     * 从正常邮件队列里面获取要发送的邮件
     */
    @Scheduled(fixedRate = MAIL_SEND_RATE)
    public void sendMail() {
        Object message = rabbitTemplate.receiveAndConvert(mailSend);
        if (message != null && message instanceof MailVO) {
            MailVO vo = (MailVO) message;
            sendMail(vo);
        }
    }

    @Scheduled(fixedRate = MAIL_RETRY_DELAY)
    public void retryMail() {
        Object message = rabbitTemplate.receiveAndConvert(mailRetry);
        if (message != null && message instanceof MailVO) {
            MailVO vo = (MailVO) message;
            logger.error("重发邮件,第{}次", vo.getRetryTimes());
            vo.setRetryTimes(vo.getRetryTimes() - 1);
            sendMail(vo);
        }
    }

    /**
     * 从邮件发送失败队列(延时队列)里面获取message发送到指定的邮箱
     */
    @Scheduled(fixedRate = MAIL_RETRY_DELAY)
    public void sendFailMail() {
        Object message = rabbitTemplate.receiveAndConvert(mailFail);
        if (message != null && message instanceof MailVO) {
            MailVO vo = (MailVO) message;
            logger.error("发送失败邮件,{}", vo.getErrMsg());
            sendFailMail(vo);
        }
    }

    /**
     * @param vo 邮件内容实体
     */
    private void sendMail(MailVO vo) {
        try {
            send(vo);
            logger.info("发送成功,{}", vo.toString());
        } catch (ServiceException exp) {
            vo.setErrMsg(exp.getMessage());
            // 自动重发RETRY_TIMES次，如果失败将转移到失败队列
            if (vo.getRetryTimes() > 0) {
                rabbitTemplate.convertAndSend(mailRetryExchange, mailRetry, vo);
                logger.error("发送失败,转移到重发队列,第{}次重发,错误原因:{}", vo.getRetryTimes(), vo.getErrMsg());
            } else {
                rabbitTemplate.convertAndSend(mailFailExchange, mailFail, vo);
                logger.error("重发失败,转移到失败队列,错误原因:{}", exp.getMessage());
            }
        }
    }

    private void sendFailMail(MailVO vo) {
        MailVO newMailVO = new MailVO();
        newMailVO.setContent(vo.toString());
        List<String> to = null;
        if (StringUtils.isNotEmpty(failTo)) {
            to = Arrays.asList(failTo.split(","));
        }
        if (to == null || to.size() == 0) {
            return;
        }
        newMailVO.setTo(to);
        newMailVO.setSubject("邮件发送失败");
        send(newMailVO);
    }

    @Value("${mail.smtpServer}")
    String smtpServer;
    @Value("${mail.from}")
    String from;
    @Value("${mail.username}")
    String username;
    @Value("${mail.password}")
    String password;
    @Value("${mail.displayName}")
    String displayname;
    @Value("${mail.fail.to}")
    String failTo;


    /**
     * 发送邮件
     * 如果发送邮件频率很高，可以考虑从队列里面一次获取多条message
     * 重用mail sessions来改善发送速度
     * http://stackoverflow.com/questions/13287515/how-to-send-bulk-mails-using-javax-mail-api-efficiently-can-we-use-reuse-auth
     *
     * @param vo
     */
    private void send(MailVO vo) {
        Mail mail = new Mail(smtpServer, username, password, from, displayname);
        mail.setCopyTo(vo.getCopyTo());
        mail.setTo(vo.getTo());
        mail.setSubject(vo.getSubject());
        mail.setContent(vo.getContent());
        mail.setFilesWithName(vo.getFilesWithName());
        mail.setFile(vo.getFiles());
        mail.send();
    }
}
