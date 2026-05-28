package com.itniuma.bitinn.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * 定义系统中的用户身份级别：
 *   - USER：普通用户，默认角色
 *   - ADMIN：管理员，拥有后台管理权限
 *
 * 通过 MyBatis TypeHandler（MyBatisTypeHandlerConfig.UserRoleTypeHandler）
 * 实现与数据库 VARCHAR 字段的双向转换。
 *
 * @author aceFelix
 */
@Getter
public enum UserRole {
    /** 普通用户 */
    USER("user", "普通用户"),
    /** 管理员 */
    ADMIN("admin", "管理员");

    /** 角色编码，存入数据库 */
    private final String code;
    /** 角色中文描述 */
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Jackson 序列化时使用 code 而非枚举名
     */
    @JsonValue
    public String getCode() {
        return code;
    }

    /**
     * Jackson 反序列化和 MyBatis TypeHandler 共用此方法
     *
     * @param code 角色编码（"user" 或 "admin"），null 时默认返回 USER
     * @return 对应的枚举值
     */
    @JsonCreator
    public static UserRole fromCode(String code) {
        if (code == null) return USER;
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return USER;
    }
}
