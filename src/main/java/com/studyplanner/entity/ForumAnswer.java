package com.studyplanner.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 论坛回答实体类（对应表：forum_answer）
 */
@Data
public class ForumAnswer {

    /**
     * 回答ID
     */
    private Long id;

    /**
     * 问题ID（question_id）
     */
    private Long questionId;

    /**
     * 作者用户ID（author_id）
     */
    private Long authorId;

    /**
     * 回答内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer voteCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 创建时间（create_time）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（update_time）
     */
    private LocalDateTime updateTime;
}
