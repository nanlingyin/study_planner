package com.studyplanner.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.studyplanner.dto.PlanGenerateRequest;
import com.studyplanner.entity.PlanDetail;
import com.studyplanner.entity.StudyPlan;
import com.studyplanner.mapper.PlanDetailMapper;
import com.studyplanner.mapper.PlanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 学习计划服务类
 */
@Service
public class PlanService {
    
    @Autowired
    private PlanMapper planMapper;
    
    @Autowired
    private PlanDetailMapper planDetailMapper;
    
    @Autowired
    private LLMService llmService;
    
    /**
     * 生成学习计划（调用LLM - 登录用户使用系统配置）
     */
    @Transactional
    public StudyPlan generatePlan(Long userId, PlanGenerateRequest request) {
        // 调用LLM生成计划（使用系统配置，可选择模型）
        String llmResponse = llmService.generateStudyPlan(
                request.getGoal(),
                request.getLevel(),
                request.getDailyHours().doubleValue(),
                request.getTotalDays(),
                request.getModelName()
        );
        
        // 解析LLM返回的JSON
        JSONObject planJson = parseLLMResponse(llmResponse);
        
        // 创建计划
        StudyPlan plan = new StudyPlan();
        plan.setUserId(userId);
        
        // 处理标题：如果用户未提供或为空，则使用LLM生成的标题
        String title = request.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = planJson.getString("title");
        }
        plan.setTitle(title);
        
        plan.setGoal(request.getGoal());
        plan.setLevel(request.getLevel());
        plan.setDailyHours(request.getDailyHours());
        plan.setTotalDays(request.getTotalDays());
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(LocalDate.now().plusDays(request.getTotalDays() - 1));
        plan.setStatus("进行中");
        
        planMapper.insert(plan);
        
        // 解析并保存每日任务
        savePlanDetails(plan, planJson);
        
        return plan;
    }
    
    /**
     * 游客体验生成计划（使用自定义API配置，不保存到数据库）
     */
    public JSONObject generatePlanForGuest(PlanGenerateRequest request) {
        // 调用LLM生成计划（使用自定义配置）
        String llmResponse = llmService.generateStudyPlanWithCustomConfig(
                request.getGoal(),
                request.getLevel(),
                request.getDailyHours().doubleValue(),
                request.getTotalDays(),
                request.getCustomApiUrl(),
                request.getCustomApiKey(),
                request.getModelName()
        );
        
        // 解析并返回计划JSON（不保存）
        JSONObject planJson = parseLLMResponse(llmResponse);
        planJson.put("isGuestPlan", true);
        planJson.put("message", "游客计划仅供预览，登录后可保存计划");
        
        return planJson;
    }
    
    /**
     * 解析LLM返回的响应
     */
    private JSONObject parseLLMResponse(String llmResponse) {
        // 尝试提取JSON部分（LLM可能返回额外文字）
        String jsonStr = llmResponse;
        int jsonStart = llmResponse.indexOf("{");
        int jsonEnd = llmResponse.lastIndexOf("}");
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            jsonStr = llmResponse.substring(jsonStart, jsonEnd + 1);
        }
        return JSON.parseObject(jsonStr);
    }
    
    /**
     * 保存计划详情
     */
    private void savePlanDetails(StudyPlan plan, JSONObject planJson) {
        JSONArray dailyPlans = planJson.getJSONArray("dailyPlans");
        if (dailyPlans == null) return;
        
        for (int i = 0; i < dailyPlans.size(); i++) {
            JSONObject dayPlan = dailyPlans.getJSONObject(i);
            
            PlanDetail detail = new PlanDetail();
            detail.setPlanId(plan.getId());
            detail.setDayNumber(dayPlan.getIntValue("day"));
            detail.setContent(dayPlan.getString("content"));
            detail.setDuration(BigDecimal.valueOf(dayPlan.getDoubleValue("duration")));
            detail.setResources(dayPlan.getJSONArray("resources") != null ? 
                    dayPlan.getJSONArray("resources").toJSONString() : "[]");
            detail.setIsCompleted(0);
            
            planDetailMapper.insert(detail);
        }
    }
    
    /**
     * 获取用户的所有计划
     */
    public List<StudyPlan> getUserPlans(Long userId) {
        List<StudyPlan> plans = planMapper.findByUserId(userId);
        // 计算每个计划的进度
        for (StudyPlan plan : plans) {
            plan.setProgress(getPlanProgress(plan.getId()));
        }
        return plans;
    }
    
    /**
     * 更新计划信息
     */
    public void updatePlan(Long planId, Long userId, String title, String goal) {
        StudyPlan plan = validatePlanOwner(planId, userId);
        plan.setTitle(title);
        plan.setGoal(goal);
        planMapper.update(plan);
    }

    /**
     * 获取计划详情（包含每日任务）
     */
    public StudyPlan getPlanWithDetails(Long planId) {
        return planMapper.findById(planId);
    }
    
    /**
     * 获取计划的每日任务列表
     */
    public List<PlanDetail> getPlanDetails(Long planId) {
        return planDetailMapper.findByPlanId(planId);
    }
    
    /**
     * 更新计划状态
     */
    public void updatePlanStatus(Long planId, String status) {
        planMapper.updateStatus(planId, status);
    }
    
    /**
     * 删除计划
     */
    @Transactional
    public void deletePlan(Long planId, Long userId) {
        StudyPlan plan = validatePlanOwner(planId, userId);
        
        planDetailMapper.deleteByPlanId(planId);
        planMapper.delete(plan.getId());
    }
    
    /**
     * 获取今日任务
     */
    public PlanDetail getTodayTask(Long planId, Long userId) {
        StudyPlan plan = validatePlanOwner(planId, userId);
        
        // 计算今天是计划的第几天
        long dayNumber = LocalDate.now().toEpochDay() - plan.getStartDate().toEpochDay() + 1;
        
        if (dayNumber < 1 || dayNumber > plan.getTotalDays()) {
            return null;
        }
        
        return planDetailMapper.findByPlanIdAndDay(planId, (int) dayNumber);
    }
    
    /**
     * 获取计划进度
     */
    public double getPlanProgress(Long planId) {
        List<PlanDetail> details = planDetailMapper.findByPlanId(planId);
        if (details.isEmpty()) {
            return 0;
        }
        
        int completed = planDetailMapper.countCompletedByPlanId(planId);
        return (double) completed / details.size() * 100;
    }

    /**
     * 校验计划所有权
     */
    private StudyPlan validatePlanOwner(Long planId, Long userId) {
        StudyPlan plan = planMapper.findById(planId);
        if (plan == null) {
            throw new IllegalArgumentException("计划不存在");
        }

        if (userId != null && !plan.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该计划");
        }

        return plan;
    }
}
