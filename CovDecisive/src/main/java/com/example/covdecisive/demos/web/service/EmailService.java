package com.example.covdecisive.demos.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final String from = "985244569@qq.com"; // 你的QQ邮箱（需与配置一致）

    public void send(String to, String subject, String text) {
        // ✅ 控制台输出邮箱地址，方便调试
        System.out.println("准备发送验证码到邮箱：" + to);

        // ✅ 邮箱格式校验
        if (to == null || !to.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            throw new IllegalArgumentException("邮箱格式不正确: " + to);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}