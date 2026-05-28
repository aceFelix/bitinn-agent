package com.itniuma.bitinn.config;

import java.util.Map;

/**
 * AI 技能系统提示词配置
 *
 * 每种技能对应一条专用的 System Prompt，
 * 在构建发给 AI 的消息列表时，替换默认的通用系统提示词，
 * 让 AI 以特定的角色、风格和结构来回答问题。
 *
 * 使用方式：
 *   ChatRequest.skill = "article"  →  buildMessagesWithBudget 读取 SKILL_PROMPTS.get("article")
 *
 * 扩展方式：
 *   新增技能时，在 SKILL_PROMPTS 中添加对应的 key 和 Prompt 即可，无需改其他代码。
 *
 * @author aceFelix
 */
public class SkillPromptConfig {

    public static final String DEFAULT_PROMPT = "你是 BitInn 开发者社区的 AI 助手，名叫小B。请热心专业地回答用户的技术问题。";

    public static final Map<String, String> SKILL_PROMPTS = Map.of(
        "article", """
            你是一位资深技术写作者，擅长撰写技术文章、博客、教程和产品文档。

            写作要求：
            1. 结构清晰：使用标题、小标题划分层次，逻辑递进
            2. 内容专业：知识准确，引用有据，避免空话套话
            3. 代码示例：涉及编码的话题必须提供可运行的代码片段
            4. 读者友好：用平实的语言解释复杂概念，适当使用类比
            5. 格式美观：善用列表、表格、引用块等 Markdown 元素

            输出结构建议：
            1. 引言（背景 + 本文目标）
            2. 核心概念与原理
            3. 实战步骤 / 详细内容
            4. 最佳实践与注意事项
            5. 总结与展望"""
    );

    /**
     * 根据技能类型获取对应的系统提示词
     *
     * @param skill 技能类型 key（如 "article"），为 null 或空字符串时返回默认 Prompt
     * @return 系统提示词文本
     */
    public static String getPrompt(String skill) {
        if (skill == null || skill.isEmpty()) {
            return DEFAULT_PROMPT;
        }
        return SKILL_PROMPTS.getOrDefault(skill, DEFAULT_PROMPT);
    }
}