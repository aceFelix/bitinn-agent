package com.itniuma.bitinn.config;

import com.itniuma.bitinn.enums.UserRole;
import com.itniuma.bitinn.enums.UserStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.context.annotation.Configuration;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis 枚举类型转换器配置
 *
 * 将 Java 枚举与数据库 VARCHAR 字段互相转换，避免在 SQL 和业务代码中硬编码字符串。
 * 例如：数据库存 "admin" → 自动映射为 UserRole.ADMIN；写 Java 枚举 → 自动转为 "admin" 写入 DB。
 *
 * 原理：
 *   MyBatis 的 TypeHandler 机制拦截 JDBC 参数的设置和结果集的读取，
 *   在枚举的 getCode() / fromCode() 方法之间做双向转换。
 *
 * 已注册的类型处理器：
 *   - UserRoleTypeHandler：用户角色（admin / user / moderator）
 *   - UserStatusTypeHandler：用户状态（active / disabled / banned）
 *
 * @author aceFelix
 */
@Configuration
public class MyBatisTypeHandlerConfig {

    /**
     * 用户角色枚举 <-> VARCHAR 转换
     *
     * 写库：UserRole.ADMIN → "admin"
     * 读库："user" → UserRole.USER
     */
    @org.apache.ibatis.type.MappedJdbcTypes(JdbcType.VARCHAR)
    @MappedTypes(UserRole.class)
    public static class UserRoleTypeHandler extends BaseTypeHandler<UserRole> {

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, UserRole parameter, JdbcType jdbcType) throws SQLException {
            ps.setString(i, parameter.getCode());
        }

        @Override
        public UserRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
            return UserRole.fromCode(rs.getString(columnName));
        }

        @Override
        public UserRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            return UserRole.fromCode(rs.getString(columnIndex));
        }

        @Override
        public UserRole getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
            return UserRole.fromCode(cs.getString(columnIndex));
        }
    }

    /**
     * 用户状态枚举 <-> VARCHAR 转换
     *
     * 写库：UserStatus.ACTIVE → "active"
     * 读库："banned" → UserStatus.BANNED
     */
    @org.apache.ibatis.type.MappedJdbcTypes(JdbcType.VARCHAR)
    @MappedTypes(UserStatus.class)
    public static class UserStatusTypeHandler extends BaseTypeHandler<UserStatus> {

        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, UserStatus parameter, JdbcType jdbcType) throws SQLException {
            ps.setString(i, parameter.getCode());
        }

        @Override
        public UserStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
            return UserStatus.fromCode(rs.getString(columnName));
        }

        @Override
        public UserStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            return UserStatus.fromCode(rs.getString(columnIndex));
        }

        @Override
        public UserStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
            return UserStatus.fromCode(cs.getString(columnIndex));
        }
    }
}
