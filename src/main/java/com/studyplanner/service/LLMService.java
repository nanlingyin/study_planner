package com.studyplanner.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * LLM服务类 - 用于调用大语言模型API
 * 支持系统默认配置和用户自定义配置
 */
@Service
public class LLMService {
    
    // 系统默认配置
    @Value("${llm.api.base-url:}")
    private String defaultBaseUrl;
    
    @Value("${llm.api.api-key:}")
    private String defaultApiKey;
    
    @Value("${llm.api.model:Qwen/Qwen3-8B}")
    private String defaultModel;
    
    @Value("${llm.api.max-tokens:4096}")
    private Integer maxTokens;
    
    @Value("${llm.api.temperature:0.7}")
    private Double temperature;
    
    @Value("${llm.api.mock-mode:false}")
    private Boolean mockMode;
    
    // 可选模型列表
    @Value("${llm.api.available-models:Qwen/Qwen3-8B,THUDM/GLM-4-9B-0414,deepseek-ai/DeepSeek-V3}")
    private String availableModelsConfig;
    
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    
    /**
     * 获取可用的模型列表
     */
    public List<String> getAvailableModels() {
        return Arrays.asList(availableModelsConfig.split(","));
    }
    
    /**
     * 获取系统默认模型
     */
    public String getDefaultModel() {
        return defaultModel;
    }
    
    /**
     * 生成学习计划（使用系统默认配置 - 登录用户）
     */
    public String generateStudyPlan(String goal, String level, double dailyHours, int totalDays, String modelName) {
        if (mockMode) {
            return generateMockPlan(goal, level, dailyHours, totalDays);
        }
        
        String useModel = (modelName != null && !modelName.isEmpty()) ? modelName : defaultModel;
        String prompt = buildPlanPrompt(goal, level, dailyHours, totalDays);
        return chat(prompt, defaultBaseUrl, defaultApiKey, useModel);
    }
    
    /**
     * 生成学习计划（使用自定义配置 - 游客用户）
     */
    public String generateStudyPlanWithCustomConfig(String goal, String level, double dailyHours, int totalDays,
                                                     String customBaseUrl, String customApiKey, String customModel) {
        // 验证自定义配置
        if (customBaseUrl == null || customBaseUrl.isEmpty()) {
            throw new IllegalArgumentException("API URL不能为空");
        }
        if (customApiKey == null || customApiKey.isEmpty()) {
            throw new IllegalArgumentException("API Key不能为空");
        }
        if (customModel == null || customModel.isEmpty()) {
            throw new IllegalArgumentException("模型名称不能为空");
        }
        
        String prompt = buildPlanPrompt(goal, level, dailyHours, totalDays);
        return chat(prompt, customBaseUrl, customApiKey, customModel);
    }
    
    /**
     * 生成模拟的学习计划（用于测试）
     */
    private String generateMockPlan(String goal, String level, double dailyHours, int totalDays) {
        JSONObject plan = new JSONObject();
        plan.put("title", goal + " - 学习计划");
        plan.put("summary", "这是一个为期" + totalDays + "天的" + goal + "学习计划，适合" + level + "水平的学习者。");
        
        JSONArray dailyPlans = new JSONArray();
        for (int i = 1; i <= totalDays; i++) {
            JSONObject dayPlan = new JSONObject();
            dayPlan.put("day", i);
            dayPlan.put("content", "第" + i + "天学习内容：根据" + goal + "的学习目标，今天需要完成基础知识学习和练习。");
            dayPlan.put("duration", dailyHours);
            JSONArray resources = new JSONArray();
            resources.add("推荐教程资源");
            resources.add("在线练习平台");
            dayPlan.put("resources", resources);
            dailyPlans.add(dayPlan);
        }
        plan.put("dailyPlans", dailyPlans);
        
        return plan.toJSONString();
    }
    
    /**
     * AI问答（使用系统配置）
     */
    public String askQuestion(String question) {
        String prompt = "你是一个专业的学习助手，请回答以下问题：\n\n" + question;
        return chat(prompt, defaultBaseUrl, defaultApiKey, defaultModel);
    }
    
    /**
     * 多轮对话（支持消息列表）
     */
    public String chat(String baseUrl, String apiKey, String model, java.util.List<java.util.Map<String, String>> messageList) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            
            JSONArray messages = new JSONArray();
            for (java.util.Map<String, String> msg : messageList) {
                JSONObject message = new JSONObject();
                message.put("role", msg.get("role"));
                message.put("content", msg.get("content"));
                messages.add(message);
            }
            requestBody.put("messages", messages);
            
            // 确保URL格式正确
            String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
            
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                
                if (!response.isSuccessful()) {
                    throw new RuntimeException("LLM API调用失败: " + response.code() + " - " + responseBody);
                }
                
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                
                return jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
        } catch (IOException e) {
            throw new RuntimeException("调用LLM API时发生错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 调用LLM API（通用方法，支持自定义配置）
     */
    public String chat(String prompt, String baseUrl, String apiKey, String model) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);
            
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);
            
            // 确保URL格式正确
            String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
            
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                
                if (!response.isSuccessful()) {
                    throw new RuntimeException("LLM API调用失败: " + response.code() + " - " + responseBody);
                }
                
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                
                return jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
        } catch (IOException e) {
            throw new RuntimeException("调用LLM API时发生错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建学习计划生成的Prompt
     */
    private String buildPlanPrompt(String goal, String level, double dailyHours, int totalDays) {
        // 检测用户输入是否为纯英文（检测所有输入字段）
        // 如果goal和level都不包含中文，且至少有一个包含英文，则认为是英文输入
        boolean goalHasChinese = containsChinese(goal);
        boolean levelHasChinese = containsChinese(level);
        boolean goalHasEnglish = containsEnglish(goal);
        boolean levelHasEnglish = containsEnglish(level);
        
        // 如果都不包含中文，且至少有一个包含英文，则认为是英文输入
        boolean isEnglishInput = !goalHasChinese && !levelHasChinese && (goalHasEnglish || levelHasEnglish);
        String languageInstruction = isEnglishInput 
            ? "CRITICAL LANGUAGE REQUIREMENT: The user's input is entirely in English. You MUST generate the ENTIRE study plan in English ONLY. This includes: title, summary, all daily content descriptions, and all resource names. DO NOT use any Chinese characters. If you use Chinese, the response will be considered incorrect. The output language MUST match the input language."
            : "请使用中文生成学习计划。所有内容（标题、摘要、每日学习内容、推荐资源）都必须使用中文。";
        
        String formatInstruction = isEnglishInput
            ? """
                Please return the study plan in the following JSON format (ONLY JSON, no other content):
                {
                    "title": "Plan Title",
                    "summary": "Plan Summary",
                    "dailyPlans": [
                        {
                            "day": 1,
                            "content": "Detailed description of today's learning content",
                            "duration": %.1f,
                            "resources": ["Resource 1", "Resource 2"]
                        }
                    ]
                }
                """
            : """
                请按照以下JSON格式返回学习计划（注意：只返回JSON，不要有其他内容）：
                {
                    "title": "计划标题",
                    "summary": "计划概述",
                    "dailyPlans": [
                        {
                            "day": 1,
                            "content": "今日学习内容详细描述",
                            "duration": %.1f,
                            "resources": ["推荐资源1", "推荐资源2"]
                        }
                    ]
                }
                """;
        
        String requirements = isEnglishInput
            ? """
                Requirements:
                1. The plan should be progressive, from basic to advanced
                2. Daily content should be specific and actionable
                3. Recommended resources should be practical (books, websites, videos, etc.)
                4. Adjust difficulty and pace according to the base level
                5. Generate a complete %d-day plan
                """
            : """
                要求：
                1. 计划要循序渐进，由浅入深
                2. 每天的内容要具体可执行
                3. 推荐的资源要实用（可以是书籍、网站、视频等）
                4. 根据基础水平调整难度和进度
                5. 生成完整的%d天计划
                """;
        
        String roleInstruction = isEnglishInput
            ? "You are a professional learning planner. Please generate a detailed study plan based on the following information:"
            : "你是一个专业的学习规划师，请根据以下信息生成一个详细的学习计划：";
        
        String infoFormat = isEnglishInput
            ? """
                【Learning Goal】：%s
                【Base Level】：%s
                【Daily Available Time】：%.1f hours
                【Plan Duration】：%d days
                """
            : """
                【学习目标】：%s
                【基础水平】：%s
                【每日可用时间】：%.1f 小时
                【计划周期】：%d 天
                """;
        
        return String.format("%s\n\n%s\n\n%s\n\n%s\n\n%s", 
            roleInstruction, 
            String.format(infoFormat, goal, level, dailyHours, totalDays),
            languageInstruction,
            String.format(formatInstruction, dailyHours),
            String.format(requirements, totalDays));
    }
    
    /**
     * 检测字符串是否包含中文字符
     */
    private boolean containsChinese(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.chars().anyMatch(ch -> ch >= 0x4E00 && ch <= 0x9FFF);
    }
    
    /**
     * 检测字符串是否包含英文字符
     */
    private boolean containsEnglish(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.chars().anyMatch(ch -> (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'));
    }
}
