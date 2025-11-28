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
study-planner/
├── src/main/java/com/studyplanner/
│   ├── controller/    # 控制器层
│   ├── service/       # 服务层
│   ├── mapper/        # MyBatis映射
│   ├── entity/        # 实体类
│   ├── dto/           # 数据传输对象
│   └── config/        # 配置类
├── src/main/resources/
│   ├── static/        # 前端静态文件
│   ├── mapper/        # MyBatis XML
│   └── application.yml
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
- `POST /api/plan/generate` - AI生成计划
- `GET /api/plan/list` - 获取计划列表
- `GET /api/plan/{id}` - 获取计划详情
- `DELETE /api/plan/{id}` - 删除计划

### 打卡接口
- `POST /api/checkin` - 打卡签到
- `GET /api/checkin/stats` - 获取学习统计
- `GET /api/checkin/calendar` - 获取日历数据

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

### 2025-11-28
#### ♻️ 代码重构 (Refactor)
- **前端分离**: 将前端重构为 Vue 版本，实现前后端分离架构
- **资源清理**: 移除后端仓库中的旧版静态资源 (`dashboard.html`, `my-plans.html`)
- **文档更新**: 在 README 中添加前端仓库链接
#### ✨ 新增特性 (Added)
- 学习进度可视化：周/月完成率图表功能
- 新增：删除/编辑计划功能

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


- ## 计划清单

- 已完成的功能，麻烦LynngNAN填写✅ Completed
- 多语言支持-一键中英文翻译（最后进行）
- "重要/紧急计划"置顶 (🚧 In Progress)
- ~~新增：删除/编辑计划功能~~✅ Completed (2025-11-26)
- "多学会儿"功能-考虑调整一天只能打卡一次的限制，提前学习打卡后续任务
- "闲置计划"提醒功能-超过3日未打卡该计划自动提醒
- 学习论坛-供大家交流心得的平台
- ~~新增用户个性化：头像上传、个人资料编辑~~ ✅ Completed (2025-11-26)
- ~~学习进度可视化：周/月完成率图表~~✅ Completed (2025-11-28)
- 考虑可以把已学习的内容左侧深蓝色色块-->绿色/其他颜色做一个颜色区分
- 我的计划页面展示单项计划的学习进度不同步 🐛bug
- 仪表盘的图标学习进度不同步，没有连接到后端数据库 🐛bug
- 
- ~~AI学习助手输出渲染问题~~ ✅ Fixed (2025-11-26) - 添加Markdown渲染和代码高亮
- ~~修改后的每日学习时间同步问题~~ ✅ Fixed (2025-11-26) - 修复studyHours参数传递

## 📄 License

MIT License
