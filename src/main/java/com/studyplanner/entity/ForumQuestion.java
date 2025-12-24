package com.studyplanner.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 论坛问题/帖子实体类（对应表：forum_question）
 * 前端命名为 question，本质是发帖
 */
@Data
public class ForumQuestion {

    /**
     * 问题ID
     */
    private Long id;

    /**
     * 作者用户ID（author_id）
     */
    private Long authorId;

    /**
     * 标题
     */
    private String title;

    /**
     * 正文内容
     */
    private String content;

    /**
     * 是否匿名(0否/1是)
     */
    private Integer anonymous;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 回答数
     */
    private Integer answerCount;

    /**
     * 关注数
     */
    private Integer followCount;

    /**
     * 创建时间（create_time）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（update_time）
     */
    private LocalDateTime updateTime;
}
