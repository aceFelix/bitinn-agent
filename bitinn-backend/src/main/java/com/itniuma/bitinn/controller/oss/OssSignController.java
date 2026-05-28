package com.itniuma.bitinn.controller.oss;

import com.itniuma.bitinn.pojo.vo.Result;
import com.itniuma.bitinn.service.oss.OssSignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OSS 签名 URL 控制器
 *
 * 阿里云 OSS 的私有 Bucket 不允许直接通过原始 URL 访问文件，
 * 需要生成带签名的临时 URL（Presigned URL）才能让前端 <img> 标签加载图片。
 *
 * 签名 URL 默认有效期 60 分钟，过期后需重新获取。
 * 前端通过 ossSign.js 工具模块自动管理签名缓存（55 分钟刷新）。
 *
 * @author aceFelix
 */
@Slf4j
@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
public class OssSignController {

    /**
     * OSS 签名服务，封装阿里云 OSS SDK 的 Presigned URL 生成逻辑
     */
    private final OssSignService ossSignService;

    /**
     * 签名 URL 默认有效期（分钟）
     */
    private static final int DEFAULT_EXPIRE_MINUTES = 60;

    /**
     * 为单个 OSS URL 生成带签名的临时访问链接
     *
     * @param url 原始 OSS URL（如 https://bucket.oss-cn-hangzhou.aliyuncs.com/path/file.jpg）
     * @return 带签名的临时 URL，非 OSS URL 则原样返回
     */
    @GetMapping("/sign-url")
    public Result<String> getSignUrl(@RequestParam String url) {
        if (!ossSignService.isOssUrl(url)) {
            return Result.success(url);
        }
        String objectKey = ossSignService.extractObjectKey(url);
        if (objectKey == null || objectKey.isEmpty()) {
            return Result.success(url);
        }
        String signedUrl = ossSignService.generatePresignedUrl(objectKey, DEFAULT_EXPIRE_MINUTES);
        log.debug("[OSS签名] 原URL: {}, 签名URL: {}", url, signedUrl);
        return Result.success(signedUrl);
    }

    /**
     * 批量为多个 OSS URL 生成带签名的临时访问链接
     *
     * 用于 Feed 流加载时一次性获取所有文章封面的签名 URL
     *
     * @param urls OSS URL 列表
     * @return 原 URL → 签名 URL 的映射表
     */
    @PostMapping("/sign-urls")
    public Result<Map<String, String>> getSignUrls(@RequestBody List<String> urls) {
        Map<String, String> result = new HashMap<>();
        for (String url : urls) {
            if (url == null || url.isEmpty()) continue;
            if (!ossSignService.isOssUrl(url)) {
                result.put(url, url);
                continue;
            }
            String objectKey = ossSignService.extractObjectKey(url);
            if (objectKey == null || objectKey.isEmpty()) {
                result.put(url, url);
                continue;
            }
            String signedUrl = ossSignService.generatePresignedUrl(objectKey, DEFAULT_EXPIRE_MINUTES);
            result.put(url, signedUrl);
        }
        return Result.success(result);
    }
}
