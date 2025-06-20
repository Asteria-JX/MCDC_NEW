package com.example.covdecisive.demos.web.mapper;

import com.example.covdecisive.demos.web.model.TestProgram;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TestProgramMapper {
    @Insert("INSERT INTO test_programs (program_id, test_program_name, user_id, create_way) " +
            "VALUES (#{programId}, #{testProgramName}, #{userId}, #{createWay})")
    @Options(useGeneratedKeys = true, keyProperty = "testProgramId") // 获取自增主键的值
    void insertTestProgram(TestProgram testProgram);

    // 在TestProgramMapper.java中添加
//    @Select("SELECT * FROM test_programs WHERE program_id = #{programId} and user_id = #{userId}")
    @Select("SELECT test_program_id as testProgramId, program_id as programId, user_id as userId, " +
            "test_program_name as testProgramName, create_way as createWay " +
            "FROM test_programs WHERE program_id = #{programId} and user_id = #{userId}")
    TestProgram findByProgramIdAndUserId(@Param("programId") Integer programId, @Param("userId") Integer userId);

}