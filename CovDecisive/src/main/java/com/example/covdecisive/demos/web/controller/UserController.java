package com.example.covdecisive.demos.web.controller;

import com.example.covdecisive.demos.web.model.User;
import com.example.covdecisive.demos.web.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Api(tags = "API接口")
@RestController
//Spring 中的一个注解，用于支持跨域请求
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailCodeController emailCodeController;

    @ApiOperation("查询所有用户")
    @GetMapping("/getAllUsers")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/handleLogin")
    @ApiOperation("用户登录")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userService.login(username, password);
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("user_id", user.getUserId());
            data.put("role", user.getUserType());
            return ResponseEntity.ok(data);
        } else {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "用户名或密码错误"));
        }
    }


    @PostMapping("/handleRegister")
    @ApiOperation("用户注册")
    public ResponseEntity<?> handleRegister(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String confirmPassword = body.get("confirmPassword");
        String email = body.get("email");
        String emailCode = body.get("emailCode");

        // 两次密码不一致
//        if (!password.equals(confirmPassword)) {
//            return ResponseEntity.status(400).body(Map.of("success", false, "message", "两次密码不一致"));
//        }

        // 验证码错误或过期
        if (!emailCodeController.verifyCode(email, emailCode)) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "验证码错误或过期"));
        }

        // 注册新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setUserType(User.UserType.普通用户);

        User inserted = userService.insertUser(user);
        if (inserted == null) {
            return ResponseEntity.status(409).body(Map.of("success", false, "message", "用户名已存在"));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("user_id", inserted.getUserId());
        return ResponseEntity.ok(result);
    }


    @ApiOperation("删除指定用户")
    @PostMapping("/deleteUser/{user_id}")
    public void deleteUser(@PathVariable("user_id") int user_id) {
        userService.deleteUser(user_id);
    }

    @ApiOperation("添加新用户")
    @PostMapping("/insertUser/{username}/{password}")
    public void insertUser(@PathVariable("username") String username, @PathVariable("password") String password) {
        userService.insertUser1(username, password);
    }
}
