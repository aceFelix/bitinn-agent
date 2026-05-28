package com.itniuma.bitinn.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * 文件处理服务
 * 负责解析用户上传的文件，支持文档文本提取（PDF/Word/Markdown等）和图片Base64编码。
 *
 * @author aceFelix
 */
@Slf4j
@Service
public class FileProcessService {

    private final Tika tika = new Tika();

    /**
     * 使用 Apache Tika 解析文档内容（PDF/Word/Markdown/纯文本等）。
     */
    public String parseDocument(MultipartFile file) {
        try {
            String content = tika.parseToString(file.getInputStream());
            log.info("[文档解析] 文件: {}, 内容长度: {}", file.getOriginalFilename(), content.length());
            return content;
        } catch (IOException | TikaException e) {
            log.error("[文档解析] 解析失败: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("文档解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将图片文件编码为 data URI 格式的 Base64 字符串。
     */
    public String imageToBase64(MultipartFile file) {
        try {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String mimeType = file.getContentType();
            log.info("[图片编码] 文件: {}, 类型: {}, 大小: {}KB", file.getOriginalFilename(), mimeType, file.getSize() / 1024);
            return "data:" + mimeType + ";base64," + base64;
        } catch (IOException e) {
            log.error("[图片编码] 编码失败: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("图片处理失败: " + e.getMessage(), e);
        }
    }

    public boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isDocument(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;
        return contentType.equals("application/pdf")
                || contentType.equals("text/plain")
                || contentType.equals("text/markdown")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || contentType.equals("application/msword");
    }

    /**
     * 构建文件上下文描述，图片返回 Base64，文档返回解析文本，不支持格式返回提示。
     */
    public String buildFileContext(MultipartFile file) {
        if (isImage(file)) {
            return "[图片: " + file.getOriginalFilename() + "]\n" + imageToBase64(file);
        } else if (isDocument(file)) {
            String content = parseDocument(file);
            return "[文档: " + file.getOriginalFilename() + "]\n" + content;
        }
        return "[附件: " + file.getOriginalFilename() + "]（暂不支持此格式）";
    }
}
