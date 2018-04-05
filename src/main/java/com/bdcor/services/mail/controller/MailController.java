package com.bdcor.services.mail.controller;

import com.bdcor.services.mail.exception.ServiceException;
import com.bdcor.services.mail.service.MailService;
import com.bdcor.services.mail.vo.MailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Description:
 * Author: huangrupeng
 * Create: 17/5/4 上午10:45
 */
@Controller
@RequestMapping("/service/mail")
public class MailController {

    @RequestMapping
    @ResponseBody
    public String init() {
        return "hello, mail service!";
    }

    @Autowired
    MailService mailService;


    @RequestMapping("/send")
    public ResponseEntity<String> send(MailVO vo) {
        try{
            mailService.mail2queue(vo);
            return ResponseEntity.ok().body("success");
        }catch (ServiceException exp) {
            return ResponseEntity.ok().body(exp.getMessage());
        }
    }
}
