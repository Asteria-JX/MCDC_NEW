package com.example.covdecisive.demos.web.dto;

public class TestCaseResultDTO {

    private String fileName;
    private String testCode;

    public TestCaseResultDTO() {
    }

    public TestCaseResultDTO(String fileName, String testCode) {
        this.fileName = fileName;
        this.testCode = testCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }
}
