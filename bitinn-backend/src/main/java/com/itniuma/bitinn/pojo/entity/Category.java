package com.itniuma.bitinn.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章分类实体类（MySQL）
 *
 * 映射 category 表，定义文章的分类体系。
 * 分类数据量小（一般 < 20 条），前端通过 cachedRequest 缓存 10 分钟。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    /** 主键 ID，更新时必填 */
    @NotNull(groups = Update.class)
    private Integer id;

    /** 分类名称（1-15 位非空字符） */
    @NotEmpty
    @Pattern(regexp = "^\\S{1,15}$")
    private String categoryName;

    /** 分类别名（英文标识，如 tech / note） */
    @NotEmpty
    private String categoryAlias;

    /** 创建人用户 ID */
    private Integer createUser;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 添加时的校验分组 */
    public interface Add extends Default {
    }

    /** 更新时的校验分组 */
    public interface Update extends Default {
    }
}
