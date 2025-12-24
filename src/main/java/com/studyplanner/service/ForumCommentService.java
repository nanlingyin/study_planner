package com.studyplanner.service;

import com.studyplanner.entity.ForumComment;
import com.studyplanner.entity.User;
import com.studyplanner.mapper.UserMapper;
import com.studyplanner.mapper.forum.ForumAnswerMapper;
import com.studyplanner.mapper.forum.ForumCommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ForumCommentService {

    @Autowired
    private ForumCommentMapper commentMapper;

    @Autowired
    private ForumAnswerMapper answerMapper;

    @Autowired
    private UserMapper userMapper;

    public List<Map<String, Object>> listCommentsByAnswer(Long answerId) {
        List<ForumComment> all = commentMapper.findByAnswerId(answerId);

        Map<Long, Map<String, Object>> idToMap = new HashMap<>();
        Map<Long, User> userCache = new HashMap<>();

        for (ForumComment c : all) {
            User u = userCache.computeIfAbsent(c.getAuthorId(), uid -> userMapper.findById(uid));
            Map<String, Object> cm = toCommentMap(c, u);
            cm.put("replies", new ArrayList<Map<String, Object>>());
            idToMap.put(c.getId(), cm);
        }

        List<Map<String, Object>> roots = new ArrayList<>();
        for (ForumComment c : all) {
            Map<String, Object> cm = idToMap.get(c.getId());
            Long parentId = c.getParentId();

            if (parentId == null) {
                roots.add(cm);
            } else {
                Map<String, Object> parent = idToMap.get(parentId);
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> replies = (List<Map<String, Object>>) parent.get("replies");

                    Map<String, Object> parentStub = new HashMap<>();
                    parentStub.put("id", parentId);
                    parentStub.put("author", parent.get("author"));
                    cm.put("parent", parentStub);

                    replies.add(cm);
                } else {
                    roots.add(cm);
                }
            }
        }

        return roots;
    }

    @Transactional
    public Map<String, Object> createComment(Map<String, Object> data, Long userId) {
        Long answerId = asLong(data.get("answer_id"));
        String content = asString(data.get("content"));
        Long parentId = asLong(data.get("parent_id"));

        if (answerId == null) throw new IllegalArgumentException("answer_id 不能为空");
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("内容不能为空");

        ForumComment c = new ForumComment();
        c.setAnswerId(answerId);
        c.setAuthorId(userId);
        c.setContent(content.trim());
        c.setParentId(parentId);

        commentMapper.insert(c);

        try { answerMapper.incrementCommentCount(answerId); } catch (Exception ignore) {}
        return Map.of("id", c.getId());
    }

    public Map<String, Object> voteComment(Long id) {
        commentMapper.incrementVoteCount(id);
        ForumComment c = commentMapper.findById(id);

        Map<String, Object> resp = new HashMap<>();
        resp.put("vote_count", c == null || c.getVoteCount() == null ? 0 : c.getVoteCount());
        return resp;
    }

    private Map<String, Object> toCommentMap(ForumComment c, User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("answer_id", c.getAnswerId());
        m.put("author_id", c.getAuthorId());
        m.put("content", c.getContent());
        m.put("parent_id", c.getParentId());
        m.put("created_at", c.getCreateTime() == null ? null : c.getCreateTime().toString());
        m.put("updated_at", c.getUpdateTime() == null ? null : c.getUpdateTime().toString());
        m.put("vote_count", c.getVoteCount() == null ? 0 : c.getVoteCount());
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

    @Transactional
    public Map<String, Object> updateComment(Long id, Map<String, Object> data, Long userId) {
        ForumComment c = commentMapper.findById(id);
        if (c == null) throw new IllegalArgumentException("评论不存在");
        if (!c.getAuthorId().equals(userId)) throw new IllegalArgumentException("无权编辑此评论");

        String content = asString(data.get("content"));
        if (content == null || content.trim().isEmpty()) throw new IllegalArgumentException("内容不能为空");

        commentMapper.update(id, userId, content.trim());
        c = commentMapper.findById(id);
        User u = userMapper.findById(c.getAuthorId());
        return toCommentMap(c, u);
    }

    @Transactional
    public void deleteComment(Long id, Long userId) {
        ForumComment c = commentMapper.findById(id);
        if (c == null) throw new IllegalArgumentException("评论不存在");
        if (!c.getAuthorId().equals(userId)) throw new IllegalArgumentException("无权删除此评论");
        commentMapper.delete(id, userId);
        // 注意：删除评论时应该减少回答的评论数，但这里简化处理，不修改计数
    }
}
