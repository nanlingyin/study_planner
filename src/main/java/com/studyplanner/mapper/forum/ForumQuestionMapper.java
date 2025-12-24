package com.studyplanner.mapper.forum;

import com.studyplanner.entity.ForumQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumQuestionMapper {

    @Insert("INSERT INTO forum_question " +
            "(author_id, title, content, anonymous, view_count, answer_count, follow_count, create_time, update_time) " +
            "VALUES " +
            "(#{authorId}, #{title}, #{content}, #{anonymous}, 0, 0, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ForumQuestion question);

    @Select("SELECT * FROM forum_question WHERE id = #{id}")
    ForumQuestion findById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT * FROM forum_question",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "ORDER BY create_time DESC, id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<ForumQuestion> findLatest(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("keyword") String keyword
    );

    @Select("SELECT q.* FROM forum_question q " +
            "JOIN forum_question_topic qt ON q.id = qt.question_id " +
            "WHERE qt.topic_id = #{topicId} " +
            "ORDER BY q.create_time DESC, q.id DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<ForumQuestion> findByTopicId(
            @Param("topicId") Long topicId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Update("UPDATE forum_question SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    @Update("UPDATE forum_question SET answer_count = answer_count + 1 WHERE id = #{id}")
    int incrementAnswerCount(@Param("id") Long id);

    @Update("UPDATE forum_question SET answer_count = answer_count - 1 WHERE id = #{id} AND answer_count > 0")
    int decrementAnswerCount(@Param("id") Long id);

    @Update("UPDATE forum_question SET title = #{title}, content = #{content}, update_time = NOW() WHERE id = #{id} AND author_id = #{authorId}")
    int update(@Param("id") Long id, @Param("authorId") Long authorId, @Param("title") String title, @Param("content") String content);

    @Delete("DELETE FROM forum_question WHERE id = #{id} AND author_id = #{authorId}")
    int delete(@Param("id") Long id, @Param("authorId") Long authorId);

    @Select({
            "<script>",
            "SELECT q.* FROM forum_question q",
            "<if test='sort == \"hot\"'>",
            "ORDER BY (q.answer_count * 2 + q.view_count + q.follow_count) DESC, q.create_time DESC",
            "</if>",
            "<if test='sort == \"recommend\"'>",
            "ORDER BY q.answer_count DESC, q.view_count DESC, q.create_time DESC",
            "</if>",
            "<if test='sort == null or sort == \"default\"'>",
            "ORDER BY q.create_time DESC, q.id DESC",
            "</if>",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<ForumQuestion> findWithSort(
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Select("SELECT q.* FROM forum_question q " +
            "INNER JOIN forum_question_follow qf ON q.id = qf.question_id " +
            "WHERE qf.user_id = #{userId} " +
            "ORDER BY q.create_time DESC, q.id DESC " +
            "LIMIT #{limit} OFFSET #{offset}")
    List<ForumQuestion> findFollowedByUser(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Select({
            "<script>",
            "SELECT q.* FROM forum_question q",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    (q.title LIKE CONCAT('%', #{keyword}, '%') OR q.content LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "</where>",
            "ORDER BY q.create_time DESC, q.id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<ForumQuestion> searchQuestions(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Select("SELECT * FROM forum_question WHERE author_id = #{authorId} ORDER BY create_time DESC, id DESC LIMIT #{limit} OFFSET #{offset}")
    List<ForumQuestion> findByAuthorId(
            @Param("authorId") Long authorId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
