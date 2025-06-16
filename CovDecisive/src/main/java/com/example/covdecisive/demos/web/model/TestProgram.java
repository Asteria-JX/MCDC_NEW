package com.example.covdecisive.demos.web.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data; // 添加 @Data 注解

import javax.persistence.*;

@Entity
@Table(name = "test_programs")
@Data // 使用 @Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "测试项目实体")
public class TestProgram { // 从 test_programs 更名为 TestProgram
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_program_id") // 根据 SQL 结构，将 testProgramsId 更正为 test_program_id
    @ApiModelProperty(value = "测试项目ID", required = true)
    private Integer testProgramId; // 更正字段名

    @Column(name = "program_id")
    private Integer programId; // 更改为 Integer 类型，与 SQL 模式和关系一致

    @Column(name = "test_program_name", nullable = false)
    @ApiModelProperty(value = "测试用例项目名称", required = true) // 澄清描述
    private String testProgramName;

    @Column(name = "user_id")
    private Integer userId; // 更改为 Integer 类型

    @Column(name = "create_way")
    private Integer createWay; // 更改为 Integer 类型

    // @Data 会自动生成这些方法
}