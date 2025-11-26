package com.studyplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;

/**
 * 文件上传配置
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    
    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;
    
    // 实际使用的绝对路径
    private String absoluteUploadPath;
    
    /**
     * 初始化上传目录
     */
    @PostConstruct
    public void init() {
        // 获取项目根目录的绝对路径
        String userDir = System.getProperty("user.dir");
        // 构建上传目录的绝对路径
        absoluteUploadPath = Paths.get(userDir, uploadPath).toAbsolutePath().toString();
        
        System.out.println("文件上传目录: " + absoluteUploadPath);
        
        File uploadDir = new File(absoluteUploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        // 创建头像子目录
        File avatarDir = new File(absoluteUploadPath + File.separator + "avatars");
        if (!avatarDir.exists()) {
            avatarDir.mkdirs();
        }
    }
    
    /**
     * 配置静态资源映射，使上传的文件可以通过URL访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 映射到本地上传目录
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + absoluteUploadPath + "/");
    }
    
    /**
     * 获取上传目录的绝对路径
     */
    public String getUploadPath() {
        return absoluteUploadPath;
    }
    
    public String getUrlPrefix() {
        return urlPrefix;
    }
}
