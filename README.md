# 🎯 智能学习计划生成器

基于 Spring Boot + MyBatis + MySQL + LLM API 的智能学习计划平台。

## ✨ 功能特性

- 🤖 **AI智能生成计划** - 基于大语言模型，根据学习目标自动生成个性化学习计划
- 📅 **每日打卡** - 记录学习进度，培养学习习惯
- 📊 **学习统计** - 可视化展示学习数据，进度追踪
- 💬 **AI助手** - 学习过程中的智能问答助手

## 🛠️ 技术栈

- **后端**: Spring Boot 3.2 + MyBatis
- **前端**: HTML5 + CSS3 + JavaScript + Bootstrap 5
- **数据库**: MySQL 8.x
- **LLM**: DeepSeek / 通义千问 / OpenAI (可配置)

## � 前端仓库

本项目的前端 Vue 版本代码托管在：
[https://github.com/NIIT-workshop-of-SHZU/study-planner-frontend](https://github.com/NIIT-workshop-of-SHZU/study-planner-frontend)

## �📁 项目结构

```
study_planner/
├── src/main/java/com/studyplanner/
│   ├── controller/    # 控制器层
│   ├── service/       # 服务层
│   ├── mapper/        # MyBatis映射接口
│   ├── entity/        # 实体类
│   ├── dto/           # 数据传输对象
│   ├── config/        # 配置类
│   └── StudyPlannerApplication.java
├── src/main/resources/
│   └── application.yml
├── sql/
│   └── init.sql       # 数据库初始化脚本
└── pom.xml
```

## 🚀 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+

### 2. 数据库初始化

```sql
-- 执行 sql/init.sql 创建数据库和表
source sql/init.sql
```

### 3. 配置修改

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/study_planner
    username: root
    password: your_password  # 修改为你的密码

llm:
  api:
    api-key: your_api_key    # 修改为你的API Key
    
spring:
  mail:
    host: smtp.qq.com  #QQ邮箱SMTP服务器，如使用其他邮箱请修改
    port: 587
    username: 你的QQ号@qq.com  # 例如：123456789@qq.com
    password: 你的授权码  # 刚才获取的16位授权码
```

### 4. 启动项目

```bash
# 进入项目目录
cd study-planner

# Maven 编译运行
mvn spring-boot:run
```

### 5. 访问项目

打开浏览器访问: http://localhost:8080

## 📡 API接口

### 用户接口
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `POST /api/user/logout` - 用户登出
- `GET /api/user/info` - 获取用户信息
- `POST /api/user/avatar` - 上传用户头像
- `PUT /api/user/profile` - 更新个人资料
- `PUT /api/user/password` - 修改密码

### 计划接口
- `POST /api/plan/generate` - AI生成计划（支持中英文输入）
- `GET /api/plan/list` - 获取计划列表
- `GET /api/plan/{id}` - 获取计划详情
- `DELETE /api/plan/{id}` - 删除计划

### 打卡接口
- `POST /api/checkin` - 打卡签到
- `GET /api/checkin/stats` - 获取学习统计
- `GET /api/checkin/calendar` - 获取日历数据

### 论坛接口
- `GET /api/forum/question` - 获取问题列表（支持分页、排序、筛选）
- `GET /api/forum/question/{id}` - 获取问题详情
- `POST /api/forum/question` - 创建问题
- `PUT /api/forum/question/{id}` - 更新问题
- `DELETE /api/forum/question/{id}` - 删除问题
- `GET /api/forum/answer/{id}` - 获取回答详情
- `POST /api/forum/answer` - 创建回答
- `PUT /api/forum/answer/{id}` - 更新回答
- `DELETE /api/forum/answer/{id}` - 删除回答
- `POST /api/forum/answer/{id}/vote` - 点赞/取消点赞回答
- `POST /api/forum/answer/{id}/collect` - 收藏/取消收藏回答
- `GET /api/forum/comment` - 获取评论列表
- `POST /api/forum/comment` - 创建评论
- `GET /api/forum/topic` - 获取话题列表
- `POST /api/forum/topic` - 创建话题
- `GET /api/forum/search` - 搜索（问题/话题/用户）
- `GET /api/forum/my/questions` - 获取我的提问
- `GET /api/forum/my/answers` - 获取我的回答
- `GET /api/forum/my/collections` - 获取我的收藏

## 👥 团队分工

| 成员 | 职责 |
|------|------|
| 成员A | 后端 - 用户系统 |
| 成员B | 后端 - 计划管理、LLM对接 |
| 成员C | 前端 - 首页、登录注册 |
| 成员D | 前端 - 仪表盘、计划页面 |
| 成员E | 数据库、打卡系统 |
| 成员F | 测试、文档、答辩 |

## 📝 开发日志 (Changelog)

### 2025-12-24
#### ✨ 新增特性 (Added)
- **论坛功能完善**：
  - **话题管理**：支持创建自定义话题，自动处理重复话题
  - **用户内容管理**：新增"我的提问"、"我的回答"、"我的收藏"功能
  - **搜索功能**：支持问题、话题、用户的多维度搜索
  - **用户主页**：支持查看其他用户的提问、回答和收藏
- **头像上传增强**：
  - **文件大小限制**：提升至5MB
  - **自动压缩**：大于1MB的图片自动压缩到2000x2000以内
  - **1:1比例裁剪**：固定正方形裁剪，输出200x200像素
  - **数据库同步**：上传成功后自动更新用户头像字段

#### 🐛 问题修复 (Fixed)
- **LLM服务优化**：
  - 修复计划生成语言不匹配问题，支持纯英文输入时生成英文计划
  - 加强语言约束性，使用"CRITICAL"和"MUST"等强调词汇
  - 添加 `containsEnglish` 和 `containsChinese` 方法进行语言检测
- **论坛功能修复**：
  - 修复删除回答后问题回答数不减少的问题
    - 在 `ForumQuestionMapper` 中添加 `decrementAnswerCount` 方法
    - 在 `ForumAnswerService.deleteAnswer` 中调用减少回答数逻辑
  - 修复"我的提问"页面无法显示的问题
    - 确保 `ForumQuestionService.toQuestionMap` 返回 `author_id` 字段
    - 修复前端函数名错误（`truncateQuestionContent` -> `truncateContent`）
  - 修复"我的内容"页面一直加载的问题
    - 改进错误处理，确保loading状态正确更新
- **数据完整性**：
  - 确保所有问题相关接口返回 `author_id` 字段，便于前端权限控制

#### 📝 代码改进 (Improved)
- **LLMService.java**：
  - 优化 `buildPlanPrompt` 方法，根据输入语言动态调整提示词
  - 英文输入时使用英文提示词，中文输入时使用中文提示词
  - 加强语言约束，确保输出语言与输入语言一致
- **ForumAnswerService.java**：
  - 改进删除回答逻辑，确保关联数据正确更新
- **ForumQuestionMapper.java**：
  - 新增 `decrementAnswerCount` 方法，支持减少问题回答数
- **ForumQuestionService.java**：
  - 在 `toQuestionMap` 方法中确保返回 `author_id` 字段
  - 改进数据转换逻辑，保证数据完整性

#### 📋 修改文件列表
- `main/src/main/java/com/studyplanner/service/LLMService.java`
- `main/src/main/java/com/studyplanner/service/ForumAnswerService.java`
- `main/src/main/java/com/studyplanner/service/ForumQuestionService.java`
- `main/src/main/java/com/studyplanner/mapper/forum/ForumQuestionMapper.java`

### 2025-11-30
#### ✨ 新增特性 (Added)
- **计划管理高级功能**：
    - **状态筛选**：支持按计划状态（进行中/已完成）筛选计划列表
    - **关键词搜索**：支持通过计划标题或目标内容进行关键词搜索
    - **置顶功能**：支持将重要计划置顶显示，置顶计划自动排序在前
- **闲置计划邮箱提醒**：
    - **定时任务**：实现定时检查闲置计划功能（`PlanReminderService`），每分钟自动检查
    - **邮件模板**：新增闲置计划提醒邮件模板，支持个性化提醒内容
    - **用户自定义设置**：支持用户自定义闲置时间阈值和提醒间隔（默认3天闲置，12小时提醒一次）
- **数据库与配置**：
    - 更新 `user_settings` 表默认值配置（闲置时间阈值、提醒间隔）
    - 完善邮件服务配置说明和初始化脚本

#### 📝 文档更新 (Documentation)
- **项目结构文档**：更新 README 中的项目结构说明，使其与实际代码结构保持一致
- **邮件配置说明**：在快速开始部分添加邮件服务配置说明

### 2025-11-28
#### ♻️ 代码重构 (Refactor)
- **前端分离**: 将前端重构为 Vue 版本，实现前后端分离架构
- **资源清理**: 移除后端仓库中的旧版静态资源 (`dashboard.html`, `my-plans.html`)
- **文档更新**: 在 README 中添加前端仓库链接

#### ✨ 新增特性 (Added)
- **学习进度可视化**：周/月完成率图表功能
- **计划管理**：新增删除/编辑计划功能
- **数值优化**：前端统一数值显示为两位小数，提升UI美观度
- **用户体验**：
    - 导航栏新增用户头像显示
    - 优化下拉菜单交互体验
    - 个人中心新增头像预览与上传功能

#### 🐛 问题修复 (Fixed)
- **网络请求**：将 API 请求超时时间延长至 120s，解决 AI 生成计划或对话时的网络超时误报
- **计划生成**：修复创建计划时若未填写标题导致标题为空的问题（自动使用 AI 生成的标题）
- **数据同步**：修复仪表盘图表数据未连接后端数据库的问题
- **显示优化**：修复我的计划页面单项计划进度显示不同步的问题

### 2025-11-26
#### ✨ 新增特性 (Added)
- 用户头像上传功能 (`POST /api/user/avatar`)
- 个人资料编辑功能 (`PUT /api/user/profile`)
- 密码修改功能 (`PUT /api/user/password`)
- 个人资料管理页面 (`/pages/profile.html`)
- 文件上传配置类 `FileUploadConfig.java`
- 文件上传服务 `FileUploadService.java`

#### 🐛 问题修复 (Fixed)
- **SQL脚本**: 修复初始化脚本编码问题(utf8mb4)、表删除顺序及DEFAULT值语法错误
- **API路径**: 修复个人资料页面API路径重复问题 (`/api/api/...` -> `/api/...`)
- **文件上传**: 修复头像保存路径问题，改用项目绝对路径
- **打卡功能**: 修复打卡学习时长固定为2小时的问题，现已同步计划设定时长
- **AI助手**: 修复输出渲染问题，添加Markdown解析和代码高亮支持

### 2025-11-25
#### 🎉 初始化 (Init)
- 项目初始化，完成框架搭建 (Spring Boot + MyBatis + MySQL)

- 完成数据库设计和 `init.sql` 编写

- 完成计划清单制定

- 确定团队分工

  

## 计划清单


- 多语言支持-一键中英文翻译（最后进行）
- ~~"重要/紧急计划"置顶~~✅ Completed (2025-11-30)
- ~~新增：删除/编辑计划功能~~✅ Completed (2025-11-26)
- ~~"多学会儿"功能-考虑调整一天只能打卡一次的限制，提前学习打卡后续任务~~✅ Completed (2025-11-30)
- ~~"闲置计划"提醒功能-超过3日未打卡该计划自动提醒~~✅ Completed (2025-11-30)
- ~~学习论坛-供大家交流心得的平台~~ ✅ Completed (2025-12-24)
- ~~新增用户个性化：头像上传、个人资料编辑~~ ✅ Completed (2025-11-26)
- ~~学习进度可视化：周/月完成率图表~~✅ Completed (2025-11-28)
- ~~考虑可以把已学习的内容左侧深蓝色色块-->绿色/其他颜色做一个颜色区分~~✅ Completed
- 置顶计划渲染问题
- application.yml配置文件中apikey默认值问题
- ~~我的计划页面展示单项计划的学习进度不同步~~ ✅ Fixed (2025-11-28)
- ~~仪表盘的图标学习进度不同步，没有连接到后端数据库~~ ✅ Fixed (2025-11-28)
- ~~AI学习助手输出渲染问题~~ ✅ Fixed (2025-11-26) - 添加Markdown渲染和代码高亮
- ~~修改后的每日学习时间同步问题~~ ✅ Fixed (2025-11-26) - 修复studyHours参数传递

## 📄 License

MIT License

