package com.studyplanner.service;

import com.studyplanner.entity.ForumAnswer;
import com.studyplanner.entity.User;
import com.studyplanner.mapper.UserMapper;
import com.studyplanner.mapper.forum.ForumAnswerMapper;
import com.studyplanner.mapper.forum.ForumQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ForumAnswerService {

    @Autowired
    private ForumAnswerMapper answerMapper;

    @Autowired
    private ForumQuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    public List<Map<String, Object>> listAnswers(Long questionId, Integer page, Integer pageSize, String sort) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * ps;

        List<ForumAnswer> answers = answerMapper.findByQuestionId(questionId, sort, offset, ps);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumAnswer a : answers) result.add(toAnswerMap(a));
        return result;
    }

    @Transactional
    public Map<String, Object> createAnswer(Map<String, Object> data, Long userId) {
        Long questionId = asLong(data.get("question_id"));
        String content = asString(data.get("content"));

        if (questionId == null) throw new IllegalArgumentException("question_id 不能为空");
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("内容不能为空");

        ForumAnswer a = new ForumAnswer();
        a.setQuestionId(questionId);
        a.setAuthorId(userId);
        a.setContent(content.trim());

        answerMapper.insert(a);

        try { questionMapper.incrementAnswerCount(questionId); } catch (Exception ignore) {}
        return Map.of("id", a.getId());
    }

    public Map<String, Object> voteAnswer(Long id) {
        answerMapper.incrementVoteCount(id);
        ForumAnswer a = answerMapper.findById(id);

        Map<String, Object> resp = new HashMap<>();
        resp.put("vote_count", a == null || a.getVoteCount() == null ? 0 : a.getVoteCount());
        resp.put("is_voted", true);
        return resp;
    }

    // Phase3 最小：先不落库收藏（后续用 forum_answer_collect 表实现）
    public Map<String, Object> collectAnswer(Long id) {
        return Map.of("is_collected", true);
    }

    @Transactional
    public Map<String, Object> updateAnswer(Long id, Map<String, Object> data, Long userId) {
        ForumAnswer a = answerMapper.findById(id);
        if (a == null) throw new IllegalArgumentException("回答不存在");
        if (!a.getAuthorId().equals(userId)) throw new IllegalArgumentException("无权编辑此回答");

        String content = asString(data.get("content"));
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("内容不能为空");

        answerMapper.update(id, userId, content.trim());
        a = answerMapper.findById(id);
        return toAnswerMap(a);
    }

    @Transactional
    public void deleteAnswer(Long id, Long userId) {
        ForumAnswer a = answerMapper.findById(id);
        if (a == null) throw new IllegalArgumentException("回答不存在");
        if (!a.getAuthorId().equals(userId)) throw new IllegalArgumentException("无权删除此回答");
        
        Long questionId = a.getQuestionId();
        answerMapper.delete(id, userId);
        
        // 减少问题的回答数
        try { 
            questionMapper.decrementAnswerCount(questionId); 
        } catch (Exception ignore) {}
    }

    public Map<String, Object> getAnswerDetail(Long id) {
        ForumAnswer a = answerMapper.findById(id);
        if (a == null) return null;
        return toAnswerMap(a);
    }

    public List<Map<String, Object>> getUserAnswers(Long userId, Integer page, Integer pageSize) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * ps;

        List<ForumAnswer> answers = answerMapper.findByAuthorId(userId, offset, ps);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumAnswer a : answers) result.add(toAnswerMap(a));
        return result;
    }

    public List<Map<String, Object>> getUserCollections(Long userId, Integer page, Integer pageSize) {
        // 简化实现：返回空列表（需要实现收藏表查询）
        return new ArrayList<>();
    }

    private Map<String, Object> toAnswerMap(ForumAnswer a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("question_id", a.getQuestionId());
        m.put("author_id", a.getAuthorId());
        m.put("content", a.getContent());
        m.put("created_at", a.getCreateTime() == null ? null : a.getCreateTime().toString());
        m.put("updated_at", a.getUpdateTime() == null ? null : a.getUpdateTime().toString());
        m.put("vote_count", a.getVoteCount() == null ? 0 : a.getVoteCount());
        m.put("comment_count", a.getCommentCount() == null ? 0 : a.getCommentCount());
        m.put("is_voted", false);
        m.put("is_collected", false);

        User u = userMapper.findById(a.getAuthorId());
        m.put("author", toUserMap(u));
        return m;
    }

    private Map<String, Object> toUserMap(User u) {
        if (u == null) return null;
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("avatar", u.getAvatar());
        m.put("bio", null);
        return m;
    }

    private String asString(Object o) { return o == null ? null : String.valueOf(o); }

    private Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}
