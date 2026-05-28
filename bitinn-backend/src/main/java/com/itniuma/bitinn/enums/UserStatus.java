package com.itniuma.bitinn.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户状态枚举
 *
 * 定义用户账号的可用状态：
 *   - ACTIVE：正常，可以登录和使用所有功能
 *   - BANNED：封禁，无法登录
 *
 * 通过 MyBatis TypeHandler（MyBatisTypeHandlerConfig.UserStatusTypeHandler）
 * 实现与数据库 VARCHAR 字段的双向转换。
 *
 * @author aceFelix
 */
@Getter
public enum UserStatus {
    /** 正常状态 */
    ACTIVE("active", "正常"),
    /** 封禁状态 */
    BANNED("banned", "封禁");

    /** 状态编码，存入数据库 */
    private final String code;
    /** 状态中文描述 */
    private final String description;

    UserStatus(String code, String description) {
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
     * @param code 状态编码（"active" 或 "banned"），null 时默认返回 ACTIVE
     * @return 对应的枚举值
     */
    @JsonCreator
    public static UserStatus fromCode(String code) {
        if (code == null) return ACTIVE;
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
