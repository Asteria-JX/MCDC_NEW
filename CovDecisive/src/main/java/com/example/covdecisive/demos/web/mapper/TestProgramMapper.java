package com.example.covdecisive.demos.web.mapper;

import com.example.covdecisive.demos.web.model.TestProgram;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TestProgramMapper {

    @Insert("INSERT INTO test_programs (program_id, test_program_name, user_id, create_way) " +
            "VALUES (#{programId}, #{testProgramName}, #{userId}, #{createWay})")
    @Options(useGeneratedKeys = true, keyProperty = "testProgramId") // 获取自增主键的值
    void insertTestProgram(TestProgram testProgram);
}