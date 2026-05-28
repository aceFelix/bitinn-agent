package com.itniuma.bitinn.mapper.user;

import com.itniuma.bitinn.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 *
 * MyBatis 数据访问接口，SQL 定义在 UserMapper.xml 中。
 * 提供用户注册、登录、信息查询和修改的数据访问。
 * 密码通过 BCrypt 加密存储，MyBatis TypeHandler 自动处理角色和状态枚举。
 *
 * @author aceFelix
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     *
     * 用于登录验证和注册时的用户名唯一性检查
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * 用于注册时的邮箱唯一性检查
     */
    User findByEmail(@Param("email") String email);

    /** 新增用户（注册），返回自增主键 id */
    void insert(User user);

    /** 更新用户信息（昵称、简介等非 null 字段） */
    void update(User user);

    /** 更新用户头像 URL */
    void updateAvatar(@Param("id") Integer id, @Param("userPic") String userPic);

    /** 更新密码（BCrypt 加密后的密文） */
    void updatePassword(@Param("id") Integer id, @Param("password") String password);

    /** 根据主键查询用户 */
    User findById(@Param("id") Integer id);
}
