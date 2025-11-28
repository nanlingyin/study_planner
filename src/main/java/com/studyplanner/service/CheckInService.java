package com.studyplanner.service;

import com.studyplanner.entity.CheckIn;
import com.studyplanner.mapper.CheckInMapper;
import com.studyplanner.mapper.PlanDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打卡服务类
 */
@Service
public class CheckInService {
    
    @Autowired
    private CheckInMapper checkInMapper;
    
    @Autowired
    private PlanDetailMapper planDetailMapper;
    
    /**
     * 打卡签到
     */
    @Transactional
    public CheckIn checkIn(CheckIn checkIn) {
        // 设置打卡日期为今天（如果未指定）
        if (checkIn.getCheckDate() == null) {
            checkIn.setCheckDate(LocalDate.now());
        }
        
        // 保存打卡记录
        checkInMapper.insert(checkIn);
        
        // 更新任务完成状态
        planDetailMapper.updateCompleted(checkIn.getDetailId(), 1);
        
        return checkIn;
    }
    
    /**
     * 获取今日打卡状态
     */
    public List<CheckIn> getTodayCheckIns(Long userId) {
        return checkInMapper.findByUserIdAndDate(userId, LocalDate.now());
    }
    
    /**
     * 获取用户的打卡记录
     */
    public List<CheckIn> getUserCheckIns(Long userId) {
        return checkInMapper.findByUserId(userId);
    }
    
    /**
     * 获取用户某月的打卡数据（用于日历展示）
     */
    public Map<String, List<CheckIn>> getMonthlyCheckIns(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        List<CheckIn> checkIns = checkInMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        
        Map<String, List<CheckIn>> result = new HashMap<>();
        result.put("checkIns", checkIns);
        
        return result;
    }
    
    /**
     * 计算连续打卡天数
     */
    public int getStreakDays(Long userId) {
        List<LocalDate> dates = checkInMapper.findCheckInDates(userId);
        
        if (dates.isEmpty()) {
            return 0;
        }
        
        int streak = 0;
        LocalDate today = LocalDate.now();
        LocalDate expectedDate = today;
        
        for (LocalDate date : dates) {
            if (date.equals(expectedDate)) {
                streak++;
                expectedDate = expectedDate.minusDays(1);
            } else if (date.isBefore(expectedDate)) {
                // 如果今天还没打卡，从昨天开始计算
                if (streak == 0 && date.equals(today.minusDays(1))) {
                    streak = 1;
                    expectedDate = date.minusDays(1);
                } else {
                    break;
                }
            }
        }
        
        return streak;
    }
    
    /**
     * 获取学习统计数据
     */
    public Map<String, Object> getStudyStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总打卡天数
        stats.put("totalDays", checkInMapper.countCheckInDays(userId));
        
        // 总学习时长
        stats.put("totalHours", checkInMapper.sumStudyHours(userId));
        
        // 连续打卡天数
        stats.put("streakDays", getStreakDays(userId));
        
        return stats;
    }

    /**
     * 获取图表数据（本周和本月）
     */
    public Map<String, Object> getChartData(Long userId) {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        // 本周数据
        Map<String, Object> weekData = new HashMap<>();
        List<String> weekXAxis = new ArrayList<>();
        List<Double> weekSeries = new ArrayList<>();
        
        // 获取本周一
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            weekXAxis.add(getDayOfWeekCN(date.getDayOfWeek().getValue()));
            Double hours = checkInMapper.sumStudyHoursByDate(userId, date);
            weekSeries.add(hours != null ? hours : 0.0);
        }
        weekData.put("xAxis", weekXAxis);
        weekData.put("series", weekSeries);
        result.put("week", weekData);

        // 本月数据
        Map<String, Object> monthData = new HashMap<>();
        List<String> monthXAxis = new ArrayList<>();
        List<Double> monthSeries = new ArrayList<>();
        
        // 获取本月每一天
        LocalDate firstDay = today.withDayOfMonth(1);
        int daysInMonth = today.lengthOfMonth();
        
        // 为了图表美观，如果天数太多，可以每隔几天取一个点，或者全部返回
        // 这里简单起见，返回所有天数
        for (int i = 0; i < daysInMonth; i++) {
            LocalDate date = firstDay.plusDays(i);
            monthXAxis.add(date.getDayOfMonth() + "日");
            Double hours = checkInMapper.sumStudyHoursByDate(userId, date);
            monthSeries.add(hours != null ? hours : 0.0);
        }
        monthData.put("xAxis", monthXAxis);
        monthData.put("series", monthSeries);
        result.put("month", monthData);

        return result;
    }

    private String getDayOfWeekCN(int value) {
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return days[value - 1];
    }
}
