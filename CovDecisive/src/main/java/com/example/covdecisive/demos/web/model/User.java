package com.example.covdecisive.demos.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "users")
@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户实体")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @ApiModelProperty(value = "用户ID", required = true)
    private Integer userId;

    @Column(name = "username", unique = true, nullable = false)
    @ApiModelProperty(value = "用户名", required = true)
    private String username;

    @Column(name = "password", nullable = false)
    @ApiModelProperty(value = "密码", required = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    @ApiModelProperty(value = "用户类型（普通用户 / 管理员）")
    private UserType userType;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public enum UserType {
        普通用户, 管理员
    }
}