package com.bdcor.services.mail.service;

import com.bdcor.services.mail.exception.ServiceException;
import com.bdcor.services.mail.vo.MailVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * Description:
 * <pre>
 * </pre>
 * Author: huangrupeng
 * Create: 17/5/8 下午2:43
 */
@Service
public class MailService {

    private final RabbitTemplate rabbitTemplate;

    public MailService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${mail.send.exchange}")
    String mailExchange;
    @Value("${mail.send.queue}")
    String mailSend;


    public void mail2queue(MailVO vo) {
        if(vo.getTo() == null || vo.getTo().size() == 0) {
            throw new ServiceException("收件人(to)不能为空");
        }
        if(StringUtils.isEmpty(vo.getSubject())) {
            throw new ServiceException("邮件标题(subject)不能为空");
        }
        if(StringUtils.isEmpty(vo.getContent())) {
            throw new ServiceException("邮件内容(content)不能为空");
        }
        if (vo.getFilePaths() != null && vo.getFileNames() != null) {
            if (vo.getFilePaths().size() != vo.getFileNames().size()) {
                throw new ServiceException("附件名称(fileNames)必须和附件路径(filePaths)必须对应(顺序一致且数量相同)");
            }
            LinkedHashMap<String, String> filesWithName = vo.getFilesWithName();
            for (int i = 0; i < vo.getFilePaths().size(); i++) {
                filesWithName.put(vo.getFileNames().get(i), vo.getFilePaths().get(i));
            }
        }
        rabbitTemplate.convertAndSend(mailExchange, mailSend, vo);
    }
}
