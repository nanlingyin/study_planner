package com.studyplanner.service;

import com.studyplanner.dto.LoginRequest;
import com.studyplanner.dto.RegisterRequest;
import com.studyplanner.entity.User;
import com.studyplanner.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 */
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 用户注册
     */
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && userMapper.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        
        userMapper.insert(user);
        
        // 清除密码后返回
        user.setPassword(null);
        return user;
    }
    
    /**
     * 用户登录
     */
    public User login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 清除密码后返回
        user.setPassword(null);
        return user;
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        User user = userMapper.findById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
    
    /**
     * 更新用户信息
     */
    public User updateUser(User user) {
        userMapper.update(user);
        return getUserById(user.getId());
    }
    
    /**
     * 更新头像
     */
    public User updateAvatar(Long userId, String avatarUrl) {
        userMapper.updateAvatar(userId, avatarUrl);
        return getUserById(userId);
    }
    
    /**
     * 更新个人资料
     */
    public User updateProfile(Long userId, String email) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查邮箱是否被其他用户使用
        if (email != null && !email.equals(user.getEmail())) {
            User existingUser = userMapper.findByEmail(email);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new RuntimeException("该邮箱已被其他用户使用");
            }
        }
        
        user.setEmail(email);
        userMapper.update(user);
        return getUserById(userId);
    }
    
    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        userMapper.updatePassword(userId, passwordEncoder.encode(newPassword));
    }
}
