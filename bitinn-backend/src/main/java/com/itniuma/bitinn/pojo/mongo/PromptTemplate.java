package com.itniuma.bitinn.pojo.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 提示词模板实体（MongoDB）
 *
 * 存储预设的 System Prompt 模板，用户可以在 AI 对话中选择使用。
 * variables 列出模板中可替换的占位变量（如 [目标语言]、[岗位]）。
 *
 * 分类：
 *   - writing：写作类（文章、简历等）
 *   - code：编程类
 *   - analysis：分析类（数据、翻译等）
 *
 * @author aceFelix
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "prompt_templates")
public class PromptTemplate {

    /** MongoDB 自动生成的文档 ID */
    @Id
    private String id;

    /** 模板名称 */
    private String name;

    /** 模板描述 */
    private String description;

    /** 系统提示词内容 */
    private String systemPrompt;

    /** 可替换变量列表（如 "目标语言"、"岗位"） */
    private List<String> variables;

    /** 分类: "writing" / "code" / "analysis" */
    private String category;

    /** 是否公开（所有用户可见） */
    private Boolean isPublic;

    /** 使用次数 */
    private Long useCount;

    /** 创建者用户 ID */
    private Integer createdBy;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
