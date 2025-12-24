package com.studyplanner.mapper.forum;

import com.studyplanner.entity.ForumComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ForumCommentMapper {

    @Insert("INSERT INTO forum_comment " +
            "(answer_id, author_id, parent_id, content, vote_count, create_time, update_time) " +
            "VALUES " +
            "(#{answerId}, #{authorId}, #{parentId}, #{content}, 0, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ForumComment comment);

    @Select("SELECT * FROM forum_comment " +
            "WHERE answer_id = #{answerId} " +
            "ORDER BY create_time ASC, id ASC")
    List<ForumComment> findByAnswerId(@Param("answerId") Long answerId);

    @Select("SELECT * FROM forum_comment WHERE id = #{id}")
    ForumComment findById(@Param("id") Long id);

    @Update("UPDATE forum_comment SET vote_count = vote_count + 1 WHERE id = #{id}")
    int incrementVoteCount(@Param("id") Long id);

    @Update("UPDATE forum_comment SET content = #{content}, update_time = NOW() WHERE id = #{id} AND author_id = #{authorId}")
    int update(@Param("id") Long id, @Param("authorId") Long authorId, @Param("content") String content);

    @Delete("DELETE FROM forum_comment WHERE id = #{id} AND author_id = #{authorId}")
    int delete(@Param("id") Long id, @Param("authorId") Long authorId);
}
