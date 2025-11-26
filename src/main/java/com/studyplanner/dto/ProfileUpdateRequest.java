package com.studyplanner.dto;

import lombok.Data;

/**
 * 个人资料更新请求
 */
@Data
public class ProfileUpdateRequest {
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 用户名（可选，如果允许修改）
     */
    private String username;
    
    /**
     * 个人简介（预留字段）
     */
    private String bio;
}
