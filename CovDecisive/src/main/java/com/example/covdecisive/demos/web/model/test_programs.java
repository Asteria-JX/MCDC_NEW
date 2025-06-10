package com.example.covdecisive.demos.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "test_programs")
@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "测试项目实体")
public class test_programs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "testProgramsId")
    @ApiModelProperty(value = "测试项目ID", required = true)
    private Integer testProgramsId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "test_program_name", nullable = false)
    @ApiModelProperty(value = "密码", required = true)
    private String testProgramName;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "create_way")
    private int createWay;

    public Integer getTestProgramsId() {
        return testProgramsId;
    }

    public void setTestProgramsId(Integer testProgramsId) {
        this.testProgramsId = testProgramsId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getTestProgramName() {
        return testProgramName;
    }

    public void setTestProgramName(String testProgramName) {
        this.testProgramName = testProgramName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCreateWay() {
        return createWay;
    }

    public void setCreateWay(int createWay) {
        this.createWay = createWay;
    }
}