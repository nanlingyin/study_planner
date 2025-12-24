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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Map<String, Object>> listQuestions(Integer page, Integer pageSize, String keyword, Long topicId, String sort, Boolean following, Long userId) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * ps;

        List<ForumQuestion> questions;
        if (topicId != null) {
            questions = questionMapper.findByTopicId(topicId, ps, offset);
        } else if (following != null && following && userId != null) {
            questions = questionMapper.findFollowedByUser(userId, offset, ps);
        } else if (sort != null && (sort.equals("hot") || sort.equals("recommend"))) {
            questions = questionMapper.findWithSort(sort, offset, ps);
        } else {
            questions = questionMapper.findLatest(offset, ps, keyword);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumQuestion q : questions) result.add(toQuestionMap(q));
        return result;
    }

    public Map<String, Object> getQuestionDetail(Long id) {
        ForumQuestion q = questionMapper.findById(id);
        if (q == null) return null;

        try { questionMapper.incrementViewCount(id); } catch (Exception ignore) {}

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

        List<Long> topicIds = asLongList(data.get("topic_ids"));
        if (topicIds != null && !topicIds.isEmpty()) {
            List<Long> valid = new ArrayList<>();
            for (Long tid : topicIds) {
                if (tid == null) continue;
                // 检查是否是临时ID（大于1000000000000的可能是Date.now()生成的）
                // 或者是真实的话题ID
                if (tid > 1000000000000L) {
                    // 临时ID，跳过（应该在前端已经创建了话题）
                    continue;
                }
                ForumTopic topic = topicMapper.findById(tid);
                if (topic != null) {
                    valid.add(tid);
                    // 更新话题的问题数
                    try { 
                        topicMapper.incrementQuestionCount(tid); 
                    } catch (Exception ignore) {}
                }
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
        m.put("author_id", q.getAuthorId());
        m.put("created_at", q.getCreateTime() == null ? null : q.getCreateTime().toString());
        m.put("updated_at", q.getUpdateTime() == null ? null : q.getUpdateTime().toString());
        m.put("answer_count", q.getAnswerCount() == null ? 0 : q.getAnswerCount());
        m.put("view_count", q.getViewCount() == null ? 0 : q.getViewCount());
        m.put("follow_count", q.getFollowCount() == null ? 0 : q.getFollowCount());
        m.put("is_followed", false);

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

    @Transactional
    public Map<String, Object> updateQuestion(Long id, Map<String, Object> data, Long userId) {
        ForumQuestion q = questionMapper.findById(id);
        if (q == null) throw new IllegalArgumentException("问题不存在");
        if (!q.getAuthorId().equals(userId)) throw new IllegalArgumentException("无权编辑此问题");

        String title = asString(data.get("title"));
        String content = asString(data.get("content"));

        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("标题不能为空");
        if (content == null) content = "";

        questionMapper.update(id, userId, title.trim(), content);

        List<Long> topicIds = asLongList(data.get("topic_ids"));
        if (topicIds != null) {
            questionTopicMapper.deleteByQuestionId(id);
            if (!topicIds.isEmpty()) {
                List<Long> valid = new ArrayList<>();
                for (Long tid : topicIds) {
                    if (tid == null) continue;
                    if (topicMapper.findById(tid) != null) valid.add(tid);
                }
                if (!valid.isEmpty()) {
                    questionTopicMapper.insertBatch(id, valid);
                }
            }
        }

        return getQuestionDetail(id);
    }

    @Transactional
    public void deleteQuestion(Long id, Long userId) {
        ForumQuestion q = questionMapper.findById(id);
        if (q == null) throw new IllegalArgumentException("问题不存在");
        if (!q.getAuthorId().equals(userId)) throw new IllegalArgumentException("无权删除此问题");
        questionMapper.delete(id, userId);
    }

    public List<Map<String, Object>> searchQuestions(String keyword, Integer page, Integer pageSize) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * ps;

        List<ForumQuestion> questions = questionMapper.searchQuestions(keyword, offset, ps);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumQuestion q : questions) result.add(toQuestionMap(q));
        return result;
    }

    public List<Map<String, Object>> getUserQuestions(Long userId, Integer page, Integer pageSize) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * ps;

        List<ForumQuestion> questions = questionMapper.findByAuthorId(userId, offset, ps);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumQuestion q : questions) result.add(toQuestionMap(q));
        return result;
    }

    public Map<String, Object> getUserInfo(Long userId) {
        User u = userMapper.findById(userId);
        if (u == null) return null;

        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getUsername());
        m.put("avatar", u.getAvatar());
        m.put("bio", null); // User实体暂无bio字段
        m.put("email", u.getEmail());

        // 统计信息
        int questionCount = questionMapper.findByAuthorId(userId, 0, Integer.MAX_VALUE).size();
        m.put("question_count", questionCount);

        return m;
    }

    public List<Map<String, Object>> getFollowers(Long userId, Integer page, Integer pageSize) {
        // 简化实现：返回空列表
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getFollowing(Long userId, Integer page, Integer pageSize) {
        // 简化实现：返回空列表
        return new ArrayList<>();
    }

    public Map<String, Object> search(String keyword, String type, String sort, Integer page, Integer pageSize) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * ps;

        Map<String, Object> result = new HashMap<>();

        if (type == null || type.equals("all") || type.equals("question")) {
            List<ForumQuestion> questions = questionMapper.searchQuestions(keyword, offset, ps);
            List<Map<String, Object>> questionList = new ArrayList<>();
            for (ForumQuestion q : questions) questionList.add(toQuestionMap(q));
            result.put("questions", questionList);
        }

        if (type == null || type.equals("all") || type.equals("topic")) {
            List<ForumTopic> topics = topicMapper.searchTopics(keyword, offset, ps);
            List<Map<String, Object>> topicList = new ArrayList<>();
            for (ForumTopic t : topics) {
                Map<String, Object> tm = new HashMap<>();
                tm.put("id", t.getId());
                tm.put("name", t.getName());
                tm.put("description", t.getDescription());
                tm.put("follow_count", t.getFollowCount());
                tm.put("question_count", t.getQuestionCount());
                topicList.add(tm);
            }
            result.put("topics", topicList);
        }

        if (type == null || type.equals("all") || type.equals("user")) {
            List<User> users = userMapper.searchUsers(keyword, offset, ps);
            List<Map<String, Object>> userList = new ArrayList<>();
            for (User u : users) userList.add(toUserMap(u));
            result.put("users", userList);
        }

        return result;
    }

    public List<String> getSuggestions(String keyword) {
        // 简化实现：返回空列表
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getMyFollowing(Long userId, Integer page, Integer pageSize) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * ps;

        List<ForumQuestion> questions = questionMapper.findFollowedByUser(userId, offset, ps);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumQuestion q : questions) result.add(toQuestionMap(q));
        return result;
    }
}
