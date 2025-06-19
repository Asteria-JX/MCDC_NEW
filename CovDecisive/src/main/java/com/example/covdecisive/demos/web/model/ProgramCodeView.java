package com.example.covdecisive.demos.web.model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data; // 添加 @Data 注解
import javax.persistence.*;

@Data // 使用 @Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "program_code_view")
public class ProgramCodeView {
    @Id
    @Column(name="program_id")
    private Integer program_id;

    @Column(name="file_path")
    private String file_path;

    @Column(name="code_content")
    private String code_content;

}
