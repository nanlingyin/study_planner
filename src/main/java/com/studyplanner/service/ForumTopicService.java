package com.studyplanner.service;

import com.studyplanner.entity.ForumTopic;
import com.studyplanner.mapper.forum.ForumTopicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ForumTopicService {

    @Autowired
    private ForumTopicMapper topicMapper;

    public List<Map<String, Object>> getHotTopics(int limit) {
        List<ForumTopic> topics = topicMapper.findHotTopics(limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumTopic t : topics) result.add(toTopicMap(t, false));
        return result;
    }

    public List<Map<String, Object>> getTopics(Integer page, Integer pageSize) {
        int p = (page == null || page < 1) ? 1 : page;
        int ps = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * ps;

        List<ForumTopic> topics = topicMapper.findAllPaged(offset, ps);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ForumTopic t : topics) result.add(toTopicMap(t, false));
        return result;
    }

    public Map<String, Object> getTopicDetail(Long id) {
        ForumTopic t = topicMapper.findById(id);
        if (t == null) return null;
        return toTopicMap(t, false);
    }

    private Map<String, Object> toTopicMap(ForumTopic t, boolean isFollowed) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", t.getId());
        m.put("name", t.getName());
        m.put("description", t.getDescription());
        m.put("follow_count", t.getFollowCount() == null ? 0 : t.getFollowCount());
        m.put("question_count", t.getQuestionCount() == null ? 0 : t.getQuestionCount());
        m.put("is_followed", isFollowed);
        return m;
    }
}
