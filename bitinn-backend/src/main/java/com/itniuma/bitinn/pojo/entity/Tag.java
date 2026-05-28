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
 * 标签实体类（MySQL）
 *
 * 映射 tag 表，与文章通过 article_tag 中间表实现多对多关联。
 * 每个标签有一个自定义颜色，前端以彩色 Pill 样式展示。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    /** 主键 ID，更新时必填 */
    @NotNull(groups = Update.class)
    private Integer id;

    /** 标签名（1-20 位非空字符） */
    @NotEmpty
    @Pattern(regexp = "^\\S{1,20}$")
    private String tagName;

    /** 标签颜色（HEX 格式，如 #F97316） */
    private String tagColor;

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
