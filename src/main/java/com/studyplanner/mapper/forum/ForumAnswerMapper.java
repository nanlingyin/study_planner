package com.studyplanner.mapper.forum;

import com.studyplanner.entity.ForumAnswer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumAnswerMapper {

    @Insert("INSERT INTO forum_answer " +
            "(question_id, author_id, content, vote_count, comment_count, create_time, update_time) " +
            "VALUES " +
            "(#{questionId}, #{authorId}, #{content}, 0, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ForumAnswer answer);

    @Select("SELECT * FROM forum_answer WHERE id = #{id}")
    ForumAnswer findById(@Param("id") Long id);

    @Select({
            "<script>",
            "SELECT * FROM forum_answer",
            "WHERE question_id = #{questionId}",
            "ORDER BY",
            "<choose>",
            "  <when test='sort != null and sort == \"vote_count\"'> vote_count DESC, id DESC </when>",
            "  <otherwise> create_time DESC, id DESC </otherwise>",
            "</choose>",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<ForumAnswer> findByQuestionId(
            @Param("questionId") Long questionId,
            @Param("sort") String sort,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @Update("UPDATE forum_answer SET vote_count = vote_count + 1 WHERE id = #{id}")
    int incrementVoteCount(@Param("id") Long id);

    @Update("UPDATE forum_answer SET comment_count = comment_count + 1 WHERE id = #{answerId}")
    int incrementCommentCount(@Param("answerId") Long answerId);
}
