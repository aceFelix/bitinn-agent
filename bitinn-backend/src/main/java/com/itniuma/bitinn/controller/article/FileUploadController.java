package com.itniuma.bitinn.controller.article;

import com.itniuma.bitinn.pojo.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * 文件上传控制器
 *
 * 基于 x-file-storage 框架，支持上传到阿里云 OSS。
 * 上传成功返回 OSS 访问 URL。
 *
 * 安全校验：
 *   - 文件大小限制 10MB
 *   - 文件类型白名单（图片 + 常用文档）
 *
 * 使用场景：
 *   - 文章封面图上传
 *   - 聊天附件上传（图片、文档）
 *
 * @author aceFelix
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class FileUploadController {

    /** 最大文件大小：10MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /** 允许的文件 MIME 类型白名单 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml", "image/bmp",
            "application/pdf",
            "text/plain", "text/markdown",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * x-file-storage 文件存储服务，已配置阿里云 OSS 作为默认平台
     */
    private final FileStorageService fileStorageService;

    /**
     * 上传文件到 OSS
     *
     * @param file 上传的文件（MultipartFile）
     * @return 文件在 OSS 上的访问 URL
     */
    @PostMapping("/api/upload")
    public Result<String> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.warn("[文件上传] 不支持的文件类型: {}", contentType);
            return Result.error("不支持的文件类型，仅支持图片、PDF、Word 和文本文件");
        }

        String url = fileStorageService.of(file).upload().getUrl();
        log.info("[文件上传] 文件名: {}, 大小: {}KB, 返回URL: {}",
                file.getOriginalFilename(), file.getSize() / 1024, url);
        return Result.success(url);
    }
}
