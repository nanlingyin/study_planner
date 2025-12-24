package com.studyplanner.entity;

import lombok.Data;

/**
 * 问题-话题关联实体类（对应表：forum_question_topic）
 */
@Data
public class ForumQuestionTopic {

    /**
     * 问题ID（question_id）
     */
    private Long questionId;

    /**
     * 话题ID（topic_id）
     */
    private Long topicId;
}
