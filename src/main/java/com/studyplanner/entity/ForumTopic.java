package com.studyplanner.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 论坛话题实体类（对应表：forum_topic）
 */
@Data
public class ForumTopic {

    /**
     * 话题ID
     */
    private Long id;

    /**
     * 话题名称
     */
    private String name;

    /**
     * 话题描述
     */
    private String description;

    /**
     * 关注数
     */
    private Integer followCount;

    /**
     * 问题数
     */
    private Integer questionCount;

    /**
     * 创建时间（create_time）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（update_time）
     */
    private LocalDateTime updateTime;
}
