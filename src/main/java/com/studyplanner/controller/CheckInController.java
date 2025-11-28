package com.studyplanner.controller;

import com.studyplanner.dto.ApiResponse;
import com.studyplanner.entity.CheckIn;
import com.studyplanner.service.CheckInService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 打卡控制器
 */
@RestController
@RequestMapping("/api/checkin")
public class CheckInController {
    
    @Autowired
    private CheckInService checkInService;
    
    /**
     * 打卡签到
     */
    @PostMapping
    public ApiResponse<CheckIn> checkIn(@RequestBody CheckIn checkIn, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        checkIn.setUserId(userId);
        
        try {
            CheckIn result = checkInService.checkIn(checkIn);
            return ApiResponse.success("打卡成功", result);
        } catch (Exception e) {
            return ApiResponse.error("打卡失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取今日打卡状态
     */
    @GetMapping("/today")
    public ApiResponse<List<CheckIn>> getTodayCheckIns(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        List<CheckIn> checkIns = checkInService.getTodayCheckIns(userId);
        return ApiResponse.success(checkIns);
    }
    
    /**
     * 获取打卡记录
     */
    @GetMapping("/record")
    public ApiResponse<List<CheckIn>> getCheckInRecords(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        List<CheckIn> records = checkInService.getUserCheckIns(userId);
        return ApiResponse.success(records);
    }
    
    /**
     * 获取连续打卡天数
     */
    @GetMapping("/streak")
    public ApiResponse<Integer> getStreakDays(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        int streak = checkInService.getStreakDays(userId);
        return ApiResponse.success(streak);
    }
    
    /**
     * 获取学习统计
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStudyStats(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        Map<String, Object> stats = checkInService.getStudyStats(userId);
        return ApiResponse.success(stats);
    }

    /**
     * 获取图表数据
     */
    @GetMapping("/chart-data")
    public ApiResponse<Map<String, Object>> getChartData(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }

        Map<String, Object> chartData = checkInService.getChartData(userId);
        return ApiResponse.success(chartData);
    }
    
    /**
     * 获取月度打卡数据（日历视图）
     */
    @GetMapping("/calendar")
    public ApiResponse<Map<String, List<CheckIn>>> getMonthlyCheckIns(
            @RequestParam int year,
            @RequestParam int month,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ApiResponse.unauthorized("请先登录");
        }
        
        Map<String, List<CheckIn>> data = checkInService.getMonthlyCheckIns(userId, year, month);
        return ApiResponse.success(data);
    }
}
