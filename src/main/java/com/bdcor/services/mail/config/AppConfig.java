package com.bdcor.services.mail.config;

import com.bdcor.services.mail.bean.PropertiesStaticGetter;
import com.bdcor.services.mail.task.MailTask;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: huangrupeng
 * Create: 17/5/4 下午5:16
 */
@Configuration
@EnableScheduling
public class AppConfig {

    public final static String queueName = "mail-send";

    @Value("${mail.send.exchange}")
    String mailExchange;
    @Value("${mail.send.queue}")
    String mailSend;
    @Value("${mail.fail.exchange}")
    String mailFailExchange;
    @Value("${mail.fail.queue}")
    String mailFail;
    @Value("${mail.retry.exchange}")
    String mailRetryExchange;
    @Value("${mail.retry.queue}")
    String mailRetry;

    // 正常消息
    @Bean
    Queue sendQueue() {
        return new Queue(mailSend, false);
    }

    @Bean
    TopicExchange topicExchange() {
        return new TopicExchange(mailExchange);
    }

    @Bean
    Binding bindingSendQueue(Queue sendQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(sendQueue).to(topicExchange).with(mailSend);
    }


    // 重发
    @Bean
    Queue retryQueue() {
        return new Queue(mailRetry, false);
    }

    @Bean
    CustomExchange retryExchange() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(mailRetryExchange, "x-delayed-message", true, false, args);
    }

    @Bean
    Binding bindingRetryQueue(Queue retryQueue, Exchange retryExchange) {
        return BindingBuilder.bind(retryQueue).to(retryExchange).with(mailRetry).noargs();
    }


    // 失败消息
    @Bean
    Queue failQueue() {
        return new Queue(mailFail, false);
    }

    @Bean
    CustomExchange failExchange() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(mailFailExchange, "x-delayed-message", true, false, args);
    }

    @Bean
    Binding bindingFailQueue(Queue failQueue, Exchange failExchange) {
        return BindingBuilder.bind(failQueue).to(failExchange).with(mailFail).noargs();
    }


    @Bean
    public MailTask mailTask(RabbitTemplate rabbitTemplate) {
        return new MailTask(rabbitTemplate);
    }

    @Bean
    public PropertiesStaticGetter propertiesStaticGetter(Environment env) {
        return new PropertiesStaticGetter(env);
    }

}
