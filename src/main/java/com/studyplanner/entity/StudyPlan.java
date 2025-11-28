package com.studyplanner.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习计划实体类
 */
@Data
public class StudyPlan {
    
    /**
     * 计划ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 计划标题
     */
    private String title;
    
    /**
     * 学习目标描述
     */
    private String goal;
    
    /**
     * 基础水平(零基础/初级/中级/高级)
     */
    private String level;
    
    /**
     * 每日学习时长(小时)
     */
    private BigDecimal dailyHours;
    
    /**
     * 计划总天数
     */
    private Integer totalDays;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 状态(进行中/已完成/已放弃)
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 进度(百分比) - 非数据库字段
     */
    private Double progress;
}
