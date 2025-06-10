package com.example.covdecisive.demos.web.mapper;
import java.util.List;
import com.example.covdecisive.demos.web.model.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Results(id = "UserMap", value = {
            @Result(property = "userId", column = "user_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "userType", column = "user_type")
    })
    @Select("SELECT * FROM users")
    List<User> getAllUsers();

    @ResultMap("UserMap")
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

//    @Insert("INSERT INTO users (username, password, user_type) VALUES (#{username}, #{password}, #{userType})")
//    int insertUser(User user);

    @Insert("INSERT INTO users (username, password, user_type) VALUES (#{username}, #{password}, #{userType})")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    int insertUser(User user);


    @Delete("delete from users where user_id=#{user_id}")
    void deleteUser(@Param("user_id") int userId);

    @Insert("insert into users(username, password, user_type) values (#{username}, #{password}, '普通用户')")
    void insertUser1(@Param("username") String username, @Param("password") String password);


}
