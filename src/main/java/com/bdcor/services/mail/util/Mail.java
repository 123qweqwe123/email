/**
 * Copyright (c)2015-2016 https://github.com/javahuang/rp
 * <p>
 * Licensed under Apache License,Version 1.0
 */
package com.bdcor.services.mail.util;


import com.bdcor.services.mail.bean.PropertiesStaticGetter;
import com.bdcor.services.mail.exception.ServiceException;
import com.google.common.collect.Maps;
import org.csource.common.MyException;
import org.csource.fastdfs.FastDFSUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Description:
 * User: Huang rp
 * Date: 16/1/25
 * Version: 1.0
 */
public class Mail {
    //定义发件人、收件人、SMTP服务器、用户名、密码、主题、内容等
    private String displayName;
    private List<String> to;
    private String from;
    private String smtpServer;
    private String username;
    private String password;
    private String subject;
    private String content;
    private boolean ifAuth; //服务器是否要身份认证
    private String filename = "";
    private Vector file = new Vector(); //用于保存发送附件的文件名的集合
    private LinkedHashMap<String, String> filesWithName = Maps.newLinkedHashMap();
    private List<String> copyTo;//抄送集合

    public Mail() {
    }

    public Mail(String smtpServer, String username, String password, String from, String displayname) {
        this.smtpServer = smtpServer;
        this.username = username;
        this.password = password;
        this.from = from;
        this.displayName = displayname;
    }


    /**
     * 初始化SMTP服务器地址、发送者E-mail地址、用户名、密码、接收者、主题、内容
     */
    public Mail(String smtpServer, String from, String displayName, String username, String password, List<String> to, String subject, String content) {
        this.smtpServer = smtpServer;
        this.from = from;
        this.displayName = displayName;
        this.ifAuth = true;
        this.username = username;
        this.password = password;
        this.to = to;
        this.subject = subject;
        this.content = content;
    }

    /**
     * 初始化SMTP服务器地址、发送者E-mail地址、接收者、主题、内容
     */
    public Mail(String smtpServer, String from, String displayName, List<String> to, String subject, String content) {
        this.smtpServer = smtpServer;
        this.from = from;
        this.displayName = displayName;
        this.ifAuth = false;
        this.to = to;
        this.subject = subject;
        this.content = content;
    }

    /**
     * 发送邮件
     */
    public HashMap send() throws ServiceException {
        HashMap map = new HashMap();
        map.put("state", "success");
        String message = "邮件发送成功！";
        Session session = null;
        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpServer);
        if (ifAuth) { //服务器需要身份认证
            props.put("mail.smtp.auth", "true");
            SmtpAuth smtpAuth = new SmtpAuth(username, password);
            session = Session.getDefaultInstance(props, smtpAuth);
        } else {
            props.put("mail.smtp.auth", "false");
            session = Session.getDefaultInstance(props, null);
        }
        session.setDebug(false);
        Transport trans = null;
        try {
            Message msg = new MimeMessage(session);
            try {
                Address from_address = new InternetAddress(from, displayName, "gb2312");
                msg.setFrom(from_address);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            InternetAddress[] address = new InternetAddress[to.size()];
            for (int i = 0; i < to.size(); i++) {
                address[i] = new InternetAddress(to.get(i));
            }
            msg.setRecipients(Message.RecipientType.TO, address);//设置发件人
            //设置抄送人
            if (this.getCopyTo() != null) {
                for (String cc : copyTo) {
                    InternetAddress[] ccAddress = {new InternetAddress(cc)};
                    msg.setRecipients(Message.RecipientType.CC, ccAddress);
                }
            }

            msg.setSubject(subject);
            Multipart mp = new MimeMultipart();
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(content.toString(), "text/html;charset=gb2312");
            mp.addBodyPart(mbp);
            if (!file.isEmpty()) {//有附件
                Enumeration efile = file.elements();
                while (efile.hasMoreElements()) {
                    mbp = new MimeBodyPart();
                    filename = efile.nextElement().toString(); //选择出每一个附件名
                    byte[] attach = null;
                    try {
                        Map<String, Object> fileInfo = FastDFSUtils.downloadFile1(PropertiesStaticGetter.getProperty("fastdfs.tracker.ip"), filename);
                        if(fileInfo.get("body") == null) {
                            message = "附件不存在" + filename;
                            throw new ServiceException(message);
                        }
                        attach = (byte[]) fileInfo.get("body");
                        DataSource ds = new ByteArrayDataSource(attach, "application/octet-stream");
                        mbp.setDataHandler(new DataHandler(ds)); //得到附件本身并至入BodyPart
                        String fileName = (String) fileInfo.get("filename");
                        String newName = new String(fileName.getBytes("gb2312"), "ISO8859-1");//解决中文名附件乱码
                        mbp.setFileName(newName);  //得到文件名同样至入BodyPart
                    } catch (MyException e) {
                        e.printStackTrace();
                        message = "邮件发送失败！错误原因：\n" + "从文件服务器获取附件(" + filename + ")失败!";
                        e.printStackTrace();
                        throw new ServiceException(message, e);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mp.addBodyPart(mbp);
                }
                file.removeAllElements();
            }

            if (!filesWithName.isEmpty()) { // 附件包含附件名
                for (Map.Entry<String, String> entry : filesWithName.entrySet()) {
                    mbp = new MimeBodyPart();
                    String fileName = entry.getKey();
                    String filePath = entry.getValue();
                    byte[] attach = null;
                    try {
                        attach = FastDFSUtils.downloadFile(PropertiesStaticGetter.getProperty("fastdfs.tracker.ip"), filePath);
                        if(attach == null) {
                            message = "附件不存在" + filePath;
                            throw new ServiceException(message);
                        }
                        DataSource ds = new ByteArrayDataSource(attach, "application/octet-stream");
                        mbp.setDataHandler(new DataHandler(ds)); //得到附件本身并至入BodyPart
                    } catch (MyException e) {
                        e.printStackTrace();
                        message = "邮件发送失败！错误原因：\n" + "从文件服务器获取附件(" + filename + ")失败!";
                        e.printStackTrace();
                        throw new ServiceException(message, e);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        String newName = new String(fileName.getBytes("gb2312"), "ISO8859-1");//解决中文名附件乱码
                        mbp.setFileName(newName);  //得到文件名同样至入BodyPart
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mp.addBodyPart(mbp);
                }
            }
            msg.setContent(mp); //Multipart加入到信件
            msg.setSentDate(new Date());     //设置信件头的发送日期
            //发送信件
            msg.saveChanges();
            trans = session.getTransport("smtp");
            trans.connect(smtpServer, username, password);
            trans.sendMessage(msg, msg.getAllRecipients());
            trans.close();

        } catch (AuthenticationFailedException e) {
            map.put("state", "failed");
            message = "邮件发送失败！错误原因：\n" + "身份验证错误!";
            e.printStackTrace();
            throw new ServiceException(message, e);
        } catch (MessagingException e) {
            message = "邮件发送失败！错误原因：\n" + e.getMessage();
            map.put("state", "failed");
            e.printStackTrace();
            Exception ex = null;
            if ((ex = e.getNextException()) != null) {
                System.out.println(ex.toString());
                ex.printStackTrace();
            }
            throw new ServiceException(message, e);
        }
        //System.out.println("\n提示信息:"+message);
        map.put("message", message);
        return map;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isIfAuth() {
        return ifAuth;
    }

    public void setIfAuth(boolean ifAuth) {
        this.ifAuth = ifAuth;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Vector getFile() {
        return file;
    }

    public void setFile(Vector file) {
        this.file = file;
    }

    public List<String> getCopyTo() {
        return copyTo;
    }

    public void setCopyTo(List<String> copyTo) {
        this.copyTo = copyTo;
    }

    public LinkedHashMap<String, String> getFilesWithName() {
        return filesWithName;
    }

    public void setFilesWithName(LinkedHashMap<String, String> filesWithName) {
        this.filesWithName = filesWithName;
    }
}
