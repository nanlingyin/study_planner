package com.studyplanner.mapper.forum;

import com.studyplanner.entity.ForumTopic;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumTopicMapper {

    @Select("SELECT * FROM forum_topic " +
            "ORDER BY follow_count DESC, question_count DESC, id DESC " +
            "LIMIT #{limit}")
    List<ForumTopic> findHotTopics(@Param("limit") int limit);

    @Select("SELECT * FROM forum_topic WHERE id = #{id}")
    ForumTopic findById(@Param("id") Long id);

    // 分页版（给 /api/forum/topic 用）
    @Select("SELECT * FROM forum_topic " +
            "ORDER BY follow_count DESC, question_count DESC, id DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<ForumTopic> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    // 兼容：保留原方法也行（可选）
    @Select("SELECT * FROM forum_topic ORDER BY follow_count DESC, question_count DESC, id DESC")
    List<ForumTopic> findAll();
}
