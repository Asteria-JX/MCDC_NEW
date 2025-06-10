package com.example.covdecisive.demos.web.service;
import com.example.covdecisive.demos.web.model.User;
import com.example.covdecisive.demos.web.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            System.out.println("✅ 登录成功：" + user);
            return user;
        }
        return null;
    }

//    public boolean insertUser(User user) {
//        if (userMapper.findByUsername(user.getUsername()) != null) {
//            return false; // 用户名已存在
//        }
//        return userMapper.insertUser(user) > 0;
//    }

    public User insertUser(User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            return null; // 用户名已存在
        }
        int rows = userMapper.insertUser(user);
        return rows > 0 ? user : null;
    }


    public void deleteUser(Integer id) {
        userMapper.deleteUser(id);
    }

    public void insertUser1(String username, String password) {
        userMapper.insertUser1(username, password);
    }
}
