package com.example.covdecisive.demos.web.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data; // 添加 @Data 注解

import javax.persistence.*;

@Entity
@Table(name = "test_resources")
@Data // 使用 @Data
@AllArgsConstructor
@NoArgsConstructor
public class TestResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // 假设 'id' 是 test_resources 表的主键
    private Integer id;

    @Column(name = "file_path") // 根据数据库 schema 从 'name' 更改为 'file_path'
    private String filePath;

    @Lob
    @Column(name = "code_content")
    private String codeContent;

    @Column(name = "test_program_id")
    private Integer testProgramId; // 统一为 camelCase 命名

    @Column(name = "user_id") // 直接映射为 user_id
    private Integer userId; // 统一为 camelCase 命名

    // @Data 会自动生成这些方法
}