package com.bdcor.services.mail.vo;

import com.bdcor.services.mail.task.MailTask;
import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

/**
 * Description:
 * <pre>
 *
 * </pre>
 * Author: huangrupeng
 * Create: 17/5/5 上午8:21
 */
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
    private int retryTimes = MailTask.RETRY_TIMES; // 重发次数


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

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
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

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public List<String> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public Vector getFiles() {
        return files;
    }

    public void setFiles(Vector files) {
        this.files = files;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    @Override
    public String toString() {
        return "邮件:" +
            ":</br>  收件人=" + to +
            ",</br> 抄送人=" + copyTo +
            ",</br> 邮件标题='" + subject + '\'' +
            ",</br> 邮件内容='" + content + '\'' +
            ",</br> 附件=" + files +
            ",</br> 带附件名称的附件=" + filesWithName +
            ",</br> 错误信息='" + errMsg + '\''
            ;
    }
}
