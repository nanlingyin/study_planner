package com.studyplanner.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 论坛评论/回复实体类（对应表：forum_comment）
 * parent_id 为空：评论回答；不为空：回复评论
 */
@Data
public class ForumComment {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 回答ID（answer_id）
     */
    private Long answerId;

    /**
     * 作者用户ID（author_id）
     */
    private Long authorId;

    /**
     * 父评论ID（parent_id），为空表示一级评论
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer voteCount;

    /**
     * 创建时间（create_time）
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（update_time）
     */
    private LocalDateTime updateTime;
}
