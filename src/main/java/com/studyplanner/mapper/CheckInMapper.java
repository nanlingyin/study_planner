package com.studyplanner.mapper;

import com.studyplanner.entity.CheckIn;
import org.apache.ibatis.annotations.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 打卡记录Mapper接口
 */
@Mapper
public interface CheckInMapper {
    
    /**
     * 根据ID查询打卡记录
     */
    @Select("SELECT * FROM check_in WHERE id = #{id}")
    CheckIn findById(Long id);
    
    /**
     * 查询用户某天的打卡记录
     */
    @Select("SELECT * FROM check_in WHERE user_id = #{userId} AND check_date = #{date}")
    List<CheckIn> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 查询用户的所有打卡记录
     */
    @Select("SELECT * FROM check_in WHERE user_id = #{userId} ORDER BY check_date DESC")
    List<CheckIn> findByUserId(Long userId);
    
    /**
     * 查询计划的打卡记录
     */
    @Select("SELECT * FROM check_in WHERE plan_id = #{planId} ORDER BY check_date")
    List<CheckIn> findByPlanId(Long planId);
    
    /**
     * 查询用户某个时间段的打卡记录（用于日历展示）
     */
    @Select("SELECT * FROM check_in WHERE user_id = #{userId} AND check_date BETWEEN #{startDate} AND #{endDate}")
    List<CheckIn> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                            @Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);
    
    /**
     * 插入打卡记录
     */
    @Insert("INSERT INTO check_in (user_id, plan_id, detail_id, check_date, study_hours, note, create_time) " +
            "VALUES (#{userId}, #{planId}, #{detailId}, #{checkDate}, #{studyHours}, #{note}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CheckIn checkIn);
    
    /**
     * 统计用户总打卡天数
     */
    @Select("SELECT COUNT(DISTINCT check_date) FROM check_in WHERE user_id = #{userId}")
    int countCheckInDays(Long userId);
    
    /**
     * 统计用户总学习时长
     */
    @Select("SELECT COALESCE(SUM(study_hours), 0) FROM check_in WHERE user_id = #{userId}")
    Double sumStudyHours(Long userId);

    /**
     * 统计用户某天的学习时长
     */
    @Select("SELECT SUM(study_hours) FROM check_in WHERE user_id = #{userId} AND check_date = #{date}")
    Double sumStudyHoursByDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 计算连续打卡天数
     */
    @Select("SELECT check_date FROM check_in WHERE user_id = #{userId} GROUP BY check_date ORDER BY check_date DESC")
    List<LocalDate> findCheckInDates(Long userId);
}
