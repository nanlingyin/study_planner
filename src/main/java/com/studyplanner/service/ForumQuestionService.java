package com.studyplanner.service;

import com.studyplanner.entity.ForumQuestion;
import com.studyplanner.entity.ForumTopic;
import com.studyplanner.entity.User;
import com.studyplanner.mapper.UserMapper;
import com.studyplanner.mapper.forum.ForumQuestionMapper;
import com.studyplanner.mapper.forum.ForumQuestionTopicMapper;
import com.studyplanner.mapper.forum.ForumTopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ForumQuestionService {

    @Autowired
    private ForumQuestionMapper questionMapper;

    @Autowired
    private ForumTopicMapper topicMapper;

    @Autowired
    private ForumQuestionTopicMapper questionTopicMapper;

    @Autowired
    private UserMapper userMapper;

    public List<Map<String, Object>> listQuestions(Integer page, Integer pageSize, String keyword, Long topicId) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * ps;

        List<ForumQuestion> questions = (topicId != null)
                ? questionMapper.findByTopicId(topicId, ps, offset)
                : questionMapper.findLatest(offset, ps, keyword);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumQuestion q : questions) result.add(toQuestionMap(q));
        return result;
    }

    public Map<String, Object> getQuestionDetail(Long id) {
        ForumQuestion q = questionMapper.findById(id);
        if (q == null) return null;

        try { questionMapper.incrementViewCount(id); } catch (Exception ignore) {}
        // 重新查一次保证 view_count 真实（最稳妥）
        q = questionMapper.findById(id);

        return toQuestionMap(q);
    }

    @Transactional
    public Map<String, Object> createQuestion(Map<String, Object> data, Long userId) {
        String title = asString(data.get("title"));
        String content = asString(data.get("content"));
        Integer anonymous = asInt(data.get("anonymous"));

        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("标题不能为空");
        if (content == null) content = "";

        ForumQuestion q = new ForumQuestion();
        q.setAuthorId(userId);
        q.setTitle(title.trim());
        q.setContent(content);
        q.setAnonymous(anonymous == null ? 0 : anonymous);

        questionMapper.insert(q);

        // 绑定 topic_ids（只绑定存在的话题，避免 FK 报错）
        List<Long> topicIds = asLongList(data.get("topic_ids"));
        if (topicIds != null && !topicIds.isEmpty()) {
            List<Long> valid = new ArrayList<>();
            for (Long tid : topicIds) {
                if (tid == null) continue;
                if (topicMapper.findById(tid) != null) valid.add(tid);
            }
            if (!valid.isEmpty()) {
                questionTopicMapper.insertBatch(q.getId(), valid);
            }
        }

        return Map.of("id", q.getId());
    }

    private Map<String, Object> toQuestionMap(ForumQuestion q) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", q.getId());
        m.put("title", q.getTitle());
        m.put("content", q.getContent());
        m.put("created_at", q.getCreateTime() == null ? null : q.getCreateTime().toString());
        m.put("updated_at", q.getUpdateTime() == null ? null : q.getUpdateTime().toString());
        m.put("answer_count", q.getAnswerCount() == null ? 0 : q.getAnswerCount());
        m.put("view_count", q.getViewCount() == null ? 0 : q.getViewCount());
        m.put("follow_count", q.getFollowCount() == null ? 0 : q.getFollowCount());
        m.put("is_followed", false);

        // topics
        List<ForumTopic> topics = questionTopicMapper.findTopicsByQuestionId(q.getId());
        List<Map<String, Object>> topicList = new ArrayList<>();
        for (ForumTopic t : topics) {
            Map<String, Object> tm = new HashMap<>();
            tm.put("id", t.getId());
            tm.put("name", t.getName());
            tm.put("description", t.getDescription());
            topicList.add(tm);
        }
        m.put("topics", topicList);

        // author（匿名 -> null）
        if (q.getAnonymous() != null && q.getAnonymous() == 1) {
            m.put("author", null);
        } else {
            User u = userMapper.findById(q.getAuthorId());
            m.put("author", toUserMap(u));
        }
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

    private Integer asInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private List<Long> asLongList(Object o) {
        if (o instanceof List<?> list) {
            List<Long> out = new ArrayList<>();
            for (Object it : list) {
                Long v = asLong(it);
                if (v != null) out.add(v);
            }
            return out;
        }
        return null;
    }

    private Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}
