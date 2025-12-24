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

    @Select("SELECT * FROM forum_topic WHERE name = #{name} COLLATE utf8mb4_bin")
    ForumTopic findByName(@Param("name") String name);

    @Insert("INSERT INTO forum_topic (name, description, follow_count, question_count, create_time, update_time) " +
            "VALUES (#{name}, #{description}, 0, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ForumTopic topic);

    // 分页版（给 /api/forum/topic 用）
    @Select("SELECT * FROM forum_topic " +
            "ORDER BY follow_count DESC, question_count DESC, id DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<ForumTopic> findAllPaged(@Param("offset") int offset, @Param("limit") int limit);

    // 兼容：保留原方法也行（可选）
    @Select("SELECT * FROM forum_topic ORDER BY follow_count DESC, question_count DESC, id DESC")
    List<ForumTopic> findAll();

    @Select({
            "<script>",
            "SELECT * FROM forum_topic",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    (name LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "ORDER BY follow_count DESC, question_count DESC, id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<ForumTopic> searchTopics(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Update("UPDATE forum_topic SET question_count = question_count + 1 WHERE id = #{id}")
    int incrementQuestionCount(@Param("id") Long id);
}
