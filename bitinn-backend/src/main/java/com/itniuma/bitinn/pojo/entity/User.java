package com.itniuma.bitinn.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.itniuma.bitinn.enums.UserRole;
import com.itniuma.bitinn.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类（MySQL）
 *
 * 映射 user 表，核心用户数据模型。
 * 密码字段标记 @JsonIgnore，序列化时自动排除，不会泄漏到前端。
 * role 和 status 通过 MyBatis TypeHandler 自动与数据库 VARCHAR 互转。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    /** 主键 ID */
    @NotNull
    private Integer id;
    /** 用户名 */
    private String username;
    /** 密码（BCrypt 加密），@JsonIgnore 禁止序列化到前端 */
    @JsonIgnore
    private String password;
    /** 昵称（1-10 位非空字符） */
    @NotNull
    @Pattern(regexp = "^\\S{1,10}$")
    private String nickname;
    /** 邮箱 */
    @NotNull
    @Email
    private String email;
    /** 用户头像地址 */
    private String userPic;
    /** 用户简介 */
    private String bio;
    /** 手机号 */
    private String phone;
    /** 角色（USER / ADMIN），MyBatis TypeHandler 自动转换 */
    private UserRole role;
    /** 账号状态（ACTIVE / BANNED），MyBatis TypeHandler 自动转换 */
    private UserStatus status;
    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
    /** 最后登录 IP */
    private String lastLoginIp;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
