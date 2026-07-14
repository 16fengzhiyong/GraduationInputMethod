# 煎蛋输入法 (OmeletteIME)

> 2020 年毕业设计项目 | Android 自定义输入法

## 简介

基于 Android InputMethodService，通过 JNI 调用 C 语言实现汉字检索。现已从 Java 重构为 Kotlin + Jetpack Compose + MVI 架构。

## 技术栈

- **语言**: Kotlin + C (JNI)
- **UI**: Jetpack Compose (Material3)
- **架构**: MVI (Model-View-Intent)
- **数据库**: Room
- **依赖注入**: Hilt (Dagger)
- **网络**: OkHttp + Gson
- **构建工具**: Gradle (Kotlin DSL)

## 功能

- [x] 拼音汉字输入 / 符号输入 / 数字输入
- [x] 输入法按键背景管理
- [x] 悬浮窗快捷输入 / 快捷记事本 / 日程添加 / 快捷翻译
- [x] 个人数据备份

## 项目结构

```
app/src/main/java/com/nuc/omeletteinputmethod/
├── data/           # 数据层 (Room DAO + Model + Repository)
│   ├── local/      # AppDatabase, DAOs
│   ├── model/      # 数据实体
│   └── repository/ # 仓库模式
├── di/             # Hilt 依赖注入模块
├── kernel/         # InputMethodService (IME 核心)
├── ui/             # Compose 界面
│   ├── floating/   # 悬浮窗
│   ├── keyboard/   # 键盘 (ViewModel + Screen + State)
│   ├── notepad/    # 记事本
│   ├── schedule/   # 日程
│   ├── settings/   # 设置
│   ├── shortcut/   # 快捷输入
│   ├── theme/      # Material3 主题
│   └── translate/  # 翻译
├── inputC/         # JNI 调用 C 输入引擎
└── util/           # 工具类
```

## 构建

```bash
./gradlew assembleDebug
```

需要 CMake 3.10.2+ 编译 JNI 模块。
