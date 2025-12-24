package com.studyplanner.mapper.forum;

import com.studyplanner.entity.ForumTopic;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 问题-话题关联 Mapper（对应表：forum_question_topic）
 */
@Mapper
public interface ForumQuestionTopicMapper {

    /**
     * 发帖绑定话题：批量插入（question_id + 多个 topic_id）
     */
    @Insert({
            "<script>",
            "INSERT INTO forum_question_topic (question_id, topic_id) VALUES",
            "<foreach collection='topicIds' item='topicId' separator=','>",
            "(#{questionId}, #{topicId})",
            "</foreach>",
            "</script>"
    })
    int insertBatch(
            @Param("questionId") Long questionId,
            @Param("topicIds") List<Long> topicIds
    );

    /**
     * 问题详情：查询该问题绑定的所有话题（用于前端详情页展示 TopicTag）
     */
    @Select("SELECT t.* FROM forum_topic t " +
            "JOIN forum_question_topic qt ON t.id = qt.topic_id " +
            "WHERE qt.question_id = #{questionId} " +
            "ORDER BY t.follow_count DESC, t.id DESC")
    List<ForumTopic> findTopicsByQuestionId(@Param("questionId") Long questionId);

    @Delete("DELETE FROM forum_question_topic WHERE question_id = #{questionId}")
    int deleteByQuestionId(@Param("questionId") Long questionId);
}
