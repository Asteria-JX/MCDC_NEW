package com.example.covdecisive.demos.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourceCodeFile {
    private String filePath;
    private String codeContent;
}

