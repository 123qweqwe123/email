package com.bdcor.services.mail;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * Description:
 * Author: huangrupeng
 * Create: 17/5/4 上午10:42scp
 */
@SpringBootApplication
@EnableRabbit
public class MailServiceApplication extends SpringBootServletInitializer {


    public static void main(String[] args) {
        SpringApplication.run(MailServiceApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MailServiceApplication.class);
    }
}
