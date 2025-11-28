package com.studyplanner.controller;

import com.alibaba.fastjson2.JSONObject;
import com.studyplanner.dto.ApiResponse;
import com.studyplanner.dto.PlanGenerateRequest;
import com.studyplanner.entity.PlanDetail;
import com.studyplanner.entity.StudyPlan;
import com.studyplanner.service.LLMService;
import com.studyplanner.service.PlanService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学习计划控制器
 */
@RestController
@RequestMapping("/api/plan")
public class PlanController {

    @Autowired
    private PlanService planService;

    @Autowired
    private LLMService llmService;

    /**
     * 获取可用的模型列表（登录用户）
     */
    @GetMapping("/models")
    public ApiResponse<Map<String, Object>> getAvailableModels(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        Map<String, Object> result = new HashMap<>();
        result.put("models", llmService.getAvailableModels());
        result.put("defaultModel", llmService.getDefaultModel());
        result.put("isLoggedIn", userId != null);

        return ApiResponse.success(result);
    }

    /**
     * 生成学习计划（登录用户 - 使用系统API配置）
     */
    @PostMapping("/generate")
    public ApiResponse<StudyPlan> generatePlan(@Valid @RequestBody PlanGenerateRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        try {
            StudyPlan plan = planService.generatePlan(userId, request);
            return ApiResponse.success("计划生成成功", plan);
        } catch (Exception e) {
            return ApiResponse.error("计划生成失败: " + e.getMessage());
        }
    }

    /**
     * 游客体验 - 生成学习计划（使用自定义API配置，不保存）
     */
    @PostMapping("/generate/guest")
    public ApiResponse<JSONObject> generatePlanForGuest(@RequestBody PlanGenerateRequest request) {
        // 验证必要参数
        if (request.getGoal() == null || request.getGoal().isEmpty()) {
            return ApiResponse.error("学习目标不能为空");
        }
        if (request.getCustomApiUrl() == null || request.getCustomApiUrl().isEmpty()) {
            return ApiResponse.error("API URL不能为空");
        }
        if (request.getCustomApiKey() == null || request.getCustomApiKey().isEmpty()) {
            return ApiResponse.error("API Key不能为空");
        }
        if (request.getModelName() == null || request.getModelName().isEmpty()) {
            return ApiResponse.error("模型名称不能为空");
        }

        try {
            JSONObject plan = planService.generatePlanForGuest(request);
            return ApiResponse.success("计划生成成功（游客预览）", plan);
        } catch (Exception e) {
            return ApiResponse.error("计划生成失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有计划
     */
    @GetMapping("/list")
    public ApiResponse<List<StudyPlan>> getUserPlans(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        List<StudyPlan> plans = planService.getUserPlans(userId);
        return ApiResponse.success(plans);
    }

    /**
     * 获取用户的所有计划（别名接口）
     */
    @GetMapping("/my-plans")
    public ApiResponse<List<StudyPlan>> getMyPlans(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        List<StudyPlan> plans = planService.getUserPlans(userId);
        return ApiResponse.success(plans);
    }

    /**
     * 获取计划详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getPlanDetail(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        StudyPlan plan = planService.getPlanWithDetails(id);
        if (plan == null) {
            return ApiResponse.error("计划不存在");
        }

        // 验证计划属于当前用户
        if (!plan.getUserId().equals(userId)) {
            return ApiResponse.error("无权访问该计划");
        }

        List<PlanDetail> details = planService.getPlanDetails(id);
        double progress = planService.getPlanProgress(id);

        Map<String, Object> result = new HashMap<>();
        result.put("plan", plan);
        result.put("details", details);
        result.put("progress", progress);

        return ApiResponse.success(result);
    }

    /**
     * 获取今日任务
     */
    @GetMapping("/{id}/today")
    public ApiResponse<PlanDetail> getTodayTask(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        try {
            PlanDetail task = planService.getTodayTask(id, userId);
            if (task == null) {
                return ApiResponse.error("今日没有任务或计划已结束");
            }

            return ApiResponse.success(task);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新计划状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updatePlanStatus(@PathVariable Long id, @RequestBody Map<String, String> request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        String status = request.get("status");
        planService.updatePlanStatus(id, status);
        return ApiResponse.success("状态更新成功", null);
    }

    /**
     * 删除计划
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePlan(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        try {
            planService.deletePlan(id, userId);
            return ApiResponse.success("计划删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新计划信息
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> updatePlan(@PathVariable Long id, @RequestBody Map<String, String> request,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        String title = request.get("title");
        String goal = request.get("goal");
        
        try {
            planService.updatePlan(id, userId, title, goal);
            return ApiResponse.success("计划更新成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
