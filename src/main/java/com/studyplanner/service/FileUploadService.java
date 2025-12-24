package com.studyplanner.service;

import com.studyplanner.config.FileUploadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务
 */
@Service
public class FileUploadService {
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    // 允许的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    
    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    /**
     * 上传头像
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 头像访问URL
     */
    public String uploadAvatar(MultipartFile file, Long userId) throws IOException {
        // 验证文件
        validateImageFile(file);
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        
        // 保存文件
        String subDir = "avatars";
        Path uploadPath = Paths.get(fileUploadConfig.getUploadPath(), subDir);
        
        // 确保目录存在
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 删除该用户旧的头像文件
        deleteOldAvatars(uploadPath, userId);
        
        // 保存新文件
        Path filePath = uploadPath.resolve(newFilename);
        file.transferTo(filePath.toFile());
        
        // 返回访问URL
        return fileUploadConfig.getUrlPrefix() + "/" + subDir + "/" + newFilename;
    }
    
    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的文件");
        }
        
        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小不能超过5MB");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new RuntimeException("只支持 JPG、PNG、GIF、WEBP 格式的图片");
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * 删除用户旧的头像文件
     */
    private void deleteOldAvatars(Path uploadPath, Long userId) {
        File dir = uploadPath.toFile();
        if (dir.exists() && dir.isDirectory()) {
            File[] oldFiles = dir.listFiles((d, name) -> name.startsWith("avatar_" + userId + "_"));
            if (oldFiles != null) {
                for (File oldFile : oldFiles) {
                    oldFile.delete();
                }
            }
        }
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        
        // 从URL提取相对路径
        String relativePath = fileUrl.replace(fileUploadConfig.getUrlPrefix(), "");
        Path filePath = Paths.get(fileUploadConfig.getUploadPath(), relativePath);
        
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}
