/**
 * Copyright (c)2015-2016 https://github.com/javahuang/rp
 * <p>
 * Licensed under Apache License,Version 1.0
 */
package com.bdcor.services.mail.util;

/**
 * Description:
 * User: Huang rp
 * Date: 16/1/27
 * Version: 1.0
 */
public class SmtpAuth extends javax.mail.Authenticator{
    private String username,password;

    public SmtpAuth(String username, String password){
        this.username = username;
        this.password = password;
    }
    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
        return new javax.mail.PasswordAuthentication(username,password);
    }
}
