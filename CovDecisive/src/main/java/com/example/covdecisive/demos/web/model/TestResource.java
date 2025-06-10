package com.example.covdecisive.demos.web.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "test_resources")
@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class TestResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "code_content")
    private String code_content;

    @Column(name = "test_program_id")
    private Integer test_program_id;

    @JoinColumn(name = "user_id")
    private Integer user_id;

    public String getCode_content() {
        return code_content;
    }

    public void setCode_content(String codeContent) {
        this.code_content = codeContent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTest_Program_id() {
        return test_program_id;
    }

    public void setTest_program_id(Integer test_program_id) {
        this.test_program_id = test_program_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
}