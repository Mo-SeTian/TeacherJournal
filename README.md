# 📱 授业札记

**授业札记** 是一款专为家教老师设计的 Android 应用，帮助记录和管理家教过程中的学生信息、上课记录、课时包和收入。

---

## ✨ 核心功能

### 🧑‍🎓 学生管理
- 添加/编辑/删除学生，记录姓名、电话、科目、默认上课地点
- 支持两种付费模式：**预付费（课时包）** 和 **按次付费**

### 📦 课时包管理（预付费）
- 学生预购 N 次课时包，每次上课自动扣减 1 次
- 查看课时包历史、剩余课时
- 课时不足提醒（剩余 ≤ 2 次时首页高亮提示）

### 💰 按次付费
- 每次上完课记录课时费金额
- 支持「已收费 / 待收费」状态标记
- **待收费提醒**：首页集中展示所有未收费记录，超 7 天变红

### 📋 上课记录
- 记录上课日期、时间、地点、课程内容
- 按月浏览历史记录
- 一键标记待收费记录为已收费，自动计入收入

### 📊 首页 Dashboard
- 统计卡片：学生总数、剩余课时、本月收入
- 待收费提醒列表 + 快捷标记
- 课时不足学生提醒
- 最近上课记录

---

## 🛠 技术栈

| 层 | 技术 |
|---|---|
| **UI** | Jetpack Compose + Material Design 3 |
| **数据库** | Room (SQLite) |
| **依赖注入** | Hilt |
| **导航** | Navigation Compose |
| **架构** | MVVM + Repository |
| **语言** | Kotlin |
| **最低 SDK** | 26 (Android 8.0) |

---

## 🏗️ 项目结构

```
app/src/main/java/com/teacher/journal/
├── TeacherJournalApp.kt          # @HiltAndroidApp
├── MainActivity.kt               # 入口 Activity
├── data/
│   ├── entity/                   # Room 实体
│   │   ├── Student.kt            # 学生（含付费类型）
│   │   ├── CoursePackage.kt      # 课时包
│   │   ├── SessionRecord.kt      # 上课记录（含收费状态）
│   │   └── Earning.kt            # 收入记录
│   ├── dao/                      # DAO 接口
│   ├── database/                 # AppDatabase
│   └── repository/               # Repository 层
├── di/
│   └── AppModule.kt              # Hilt 依赖注入模块
├── ui/
│   ├── theme/                    # Color / Typography / Theme
│   ├── navigation/               # 路由 + 底部导航
│   ├── home/                     # 首页 Dashboard
│   ├── student/                  # 学生列表 / 详情 / 编辑
│   ├── session/                  # 上课记录 / 新增记录
│   └── package/                  # 课时包购买
└── util/
    └── DateUtils.kt              # 日期工具类
```

---

## 🚀 构建 & 运行

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 17
- Android SDK 34

### 本地构建

```bash
# 克隆项目
git clone https://github.com/Mo-SeTian/TeacherJournal.git
cd TeacherJournal

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

### 用 Android Studio 打开
1. File → Open → 选择项目目录
2. 等待 Gradle Sync 完成
3. 连接设备或启动模拟器（API 26+）
4. 点击 Run ▶

---

## 🤖 CI/CD 自动打包

项目配置了 GitHub Actions 自动构建工作流：

- **触发**：Push 到 `main` 分支 / 手动触发 / 打 Tag
- **产物**：`TeacherJournal-{md5}.apk`
- **下载**：Actions 页面 → 构建记录 → Artifacts

---

## 📄 License

MIT License
