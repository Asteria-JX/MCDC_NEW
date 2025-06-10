package com.example.covdecisive.demos.web.mapper;
import java.util.List;

import com.example.covdecisive.demos.web.model.TestResource;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TestResourceMapper {

    @Insert("INSERT INTO test_resources (name, program_id, user_id,code_content) VALUES (#{name}, #{program_id}, #{user_id},#{code_content})")
    void insert(TestResource resource);


    @Select("SELECT * FROM test_resources where user_id=#{user_id} and program_id=#{program_id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "code_content", column = "code_content"),
            @Result(property = "program_id", column = "program_id"),
            @Result(property = "user_id", column = "user_id")
    })
    List<TestResource> getTestAll(@Param("user_id")Integer user_id,@Param("program_id")Integer program_id);

    @Select("SELECT code_content FROM test_resources WHERE name = #{name} LIMIT 1")
    String getCodeContentByName(String name);

    @Select("SELECT * FROM test_resources WHERE program_id = #{programId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "program_id", column = "program_id"),
            @Result(property = "code_content", column = "code_content")
    })
    List<TestResource> getTestResourceByProgramID(int programId);

    @Select("SELECT * FROM test_resources WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "program_id", column = "program_id"),
            @Result(property = "code_content", column = "code_content")
    })
    TestResource getTestResourceById(int id);
}
