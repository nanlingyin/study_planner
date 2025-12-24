package com.studyplanner.mapper;

import com.studyplanner.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据ID查询用户
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
    
    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);
    
    /**
     * 插入新用户
     */
    @Insert("INSERT INTO user (username, password, email, avatar, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{email}, #{avatar}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    /**
     * 更新用户信息
     */
    @Update("UPDATE user SET email = #{email}, avatar = #{avatar}, update_time = NOW() WHERE id = #{id}")
    int update(User user);
    
    /**
     * 更新头像
     */
    @Update("UPDATE user SET avatar = #{avatar}, update_time = NOW() WHERE id = #{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);
    
    /**
     * 更新密码
     */
    @Update("UPDATE user SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Select({
            "<script>",
            "SELECT * FROM user",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    (username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "ORDER BY id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
