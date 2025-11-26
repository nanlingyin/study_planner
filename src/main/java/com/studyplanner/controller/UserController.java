package com.studyplanner.controller;

import com.studyplanner.dto.ApiResponse;
import com.studyplanner.dto.LoginRequest;
import com.studyplanner.dto.ProfileUpdateRequest;
import com.studyplanner.dto.RegisterRequest;
import com.studyplanner.entity.User;
import com.studyplanner.service.FileUploadService;
import com.studyplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ApiResponse.success("注册成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<User> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        try {
            User user = userService.login(request);
            // 将用户信息存入Session
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            return ApiResponse.success("登录成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.success("登出成功", null);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ApiResponse<User> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        User user = userService.getUserById(userId);
        return ApiResponse.success(user);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public ApiResponse<User> updateUser(@RequestBody User user, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        user.setId(userId);
        User updatedUser = userService.updateUser(user);
        return ApiResponse.success("更新成功", updatedUser);
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody PasswordChangeRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        try {
            userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return ApiResponse.success("密码修改成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 密码修改请求内部类
     */
    public static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;
        
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ApiResponse<User> uploadAvatar(@RequestParam("file") MultipartFile file, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        try {
            // 上传文件并获取URL
            String avatarUrl = fileUploadService.uploadAvatar(file, userId);
            // 更新用户头像
            User user = userService.updateAvatar(userId, avatarUrl);
            return ApiResponse.success("头像上传成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(@RequestBody ProfileUpdateRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        try {
            User user = userService.updateProfile(userId, request.getEmail());
            return ApiResponse.success("资料更新成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
