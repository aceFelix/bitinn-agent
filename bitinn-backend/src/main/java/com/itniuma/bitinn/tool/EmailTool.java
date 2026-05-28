package com.itniuma.bitinn.tool;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * 邮件工具
 * 作为 Spring AI Tool 供 ReAct Agent 调用，支持发送 HTML 格式邮件。
 *
 * @author aceFelix
 */
@Slf4j
@Component
public class EmailTool {

    private final JavaMailSender mailSender;

    public EmailTool(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Tool(description = "发送邮件给指定收件人。当用户要求发送邮件、发邮件、发送内容到邮箱时使用此工具。")
    public String sendEmail(
            @ToolParam(description = "收件人邮箱地址") String to,
            @ToolParam(description = "邮件主题") String subject,
            @ToolParam(description = "邮件正文内容，支持HTML格式") String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("[EmailTool] 邮件发送成功: to={}, subject={}", to, subject);
            return "邮件已成功发送至 " + to;
        } catch (Exception e) {
            log.error("[EmailTool] 邮件发送失败: to={}, error={}", to, e.getMessage(), e);
            return "邮件发送失败: " + e.getMessage();
        }
    }
}
