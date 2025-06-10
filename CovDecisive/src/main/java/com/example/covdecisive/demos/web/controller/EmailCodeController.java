package com.example.covdecisive.demos.web.controller;

import com.example.covdecisive.demos.web.service.EmailService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@Api(tags = "邮箱验证码接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmailCodeController {

    @Autowired
    private EmailService emailService;

    // 临时存储邮箱验证码（可替换为 Redis）
    private final Map<String, CodeEntry> emailCodeMap = new HashMap<>();

    @PostMapping("/sendEmailCode")
    public ResponseEntity<?> sendEmailCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        // ✅ 控制台输出用于调试
        System.out.println("接收到发送验证码请求，目标邮箱: " + email);

        // 生成验证码
        String code = String.valueOf(new Random().nextInt(900000) + 100000); // 6位验证码
        emailCodeMap.put(email, new CodeEntry(code, Instant.now()));

        // ✅ 控制台打印验证码（测试阶段使用）
        System.out.println("发送的验证码为: " + code);

        try {
            emailService.send(email, "验证码", "您的验证码是：" + code);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace(); // 控制台输出错误
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "邮件发送失败：" + e.getMessage()));
        }
    }

    public boolean verifyCode(String email, String inputCode) {
        CodeEntry entry = emailCodeMap.get(email);
        if (entry == null) return false;
        // 判断是否超过5分钟（300秒）
        return entry.getCode().equals(inputCode) && Instant.now().minusSeconds(300).isBefore(entry.getTime());
    }

    // 内部类用于验证码及其时间
    static class CodeEntry {
        private final String code;
        private final Instant time;

        public CodeEntry(String code, Instant time) {
            this.code = code;
            this.time = time;
        }

        public String getCode() { return code; }
        public Instant getTime() { return time; }
    }
}