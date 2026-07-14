# 煎蛋输入法 (OmeletteIME)

> 轻量、高效的 Android 自定义中文输入法

## 简介

煎蛋输入法是一款基于 Android InputMethodService 的自定义输入法应用，通过 JNI 调用 C 语言实现拼音检索（基于 Double-Array Trie 数据结构），提供完整的汉字输入能力及悬浮窗辅助工具。

项目采用 Kotlin + Jetpack Compose + MVI 架构，模块化分层设计。

## 技术栈

| 层面 | 技术 |
|------|------|
| 语言 | Kotlin + C (JNI) |
| UI | Jetpack Compose + Material3 |
| 架构 | MVI (Model-View-Intent) |
| 数据库 | Room |
| 依赖注入 | Hilt (Dagger) |
| 网络 | OkHttp + Gson |
| 字典检索 | Double-Array Trie (darts) |
| 代码检查 | ktlint + detekt |

## 功能

### 核心输入
- [x] 拼音全键盘汉字输入（26键）
- [x] 符号输入（12类符号：数学、希腊、表情等）
- [x] 数字键盘输入
- [x] 候选词联想（基于二元组 bigram）
- [ ] 笔画输入
- [ ] 手写输入
- [ ] 语音输入

### 键盘体验
- [x] 按键背景主题切换
- [ ] 键盘高度/布局自定义
- [ ] 按键震动/音效反馈
- [ ] 单手/分离键盘模式
- [ ] 夜间模式自动切换

### 悬浮窗工具
- [x] 快捷短语输入
- [x] 快捷记事本
- [x] 日程管理
- [x] 快捷翻译
- [ ] 剪贴板历史
- [ ] 计算器

### 用户系统
- [x] 本地数据存储
- [ ] 云端词库同步
- [ ] 用户词库自学习（基于输入习惯）
- [ ] 多设备词库迁移
- [ ] 输入统计与热力图

### 工程质量
- [x] MVI 架构重构（Java → Kotlin）
- [x] Room 持久化层
- [x] Hilt 依赖注入
- [x] 单元测试（JUnit4 + MockK）
- [ ] Gradle 升级（当前 5.4.1 → 8.x+）
- [ ] CI/CD 流水线
- [ ] Android 集成测试
- [ ] 性能基准测试
- [ ] 无障碍辅助适配

## 整体 TODO

### v1.1 — 构建与质量
- [ ] 升级 Gradle wrapper 至 8.x（适配 AGP 8.5.0 + Kotlin 2.0.0）
- [ ] 配置 GitHub Actions CI（ktlint + detekt + unit test）
- [ ] 补充 UI 集成测试
- [ ] 添加性能基准测试（字典检索耗时）

### v1.2 — 核心输入增强
- [ ] 词库扩充与优化
- [ ] 用户词库自学习
- [ ] 输入统计与热力图展示
- [ ] 笔画输入模块

### v1.3 — 用户体验
- [ ] 键盘布局自定义（高度、一键多字母）
- [ ] 按键震动/音效反馈
- [ ] 夜间模式自动切换
- [ ] 单手模式

### v1.4 — 云端与生态
- [ ] 用户账号体系
- [ ] 云端词库同步
- [ ] 剪贴板历史管理
- [ ] 语音/手写输入

## 项目结构

```
app/src/main/java/com/nuc/omeletteinputmethod/
├── data/            数据层
│   ├── local/       Room 数据库 + DAO + 字典部署
│   ├── model/       数据实体
│   └── repository/  数据仓库
├── di/              Hilt 依赖注入模块
├── kernel/          InputMethodService（IME 核心）
├── ui/              Jetpack Compose 界面
│   ├── floating/    悬浮窗
│   ├── keyboard/    键盘
│   ├── notepad/     记事本
│   ├── schedule/    日程
│   ├── settings/    设置
│   ├── shortcut/    快捷输入
│   ├── theme/       主题
│   └── translate/   翻译
├── inputC/          JNI 桥接
└── util/            工具类

app/src/main/cpp/          C 层（JNI）
├── darts.h            Double-Array Trie 数据结构
├── dict_engine.cpp    字典检索引擎
├── native-lib.cpp     JNI 导出函数
├── trie.cpp           Trie 辅助实现
└── utf16char.h        UTF-16 工具

tools/                    词典构建工具
├── build_dictionary.py   基于 Rime 词库生成 dict.db
└── dict_builder.py       内置词库构建器
```

## 构建与运行

```bash
# 构建调试版
./gradlew assembleDebug

# 运行代码检查
./gradlew ktlintCheck detekt

# 运行单元测试
./gradlew test
```

需要 CMake 3.10.2+ 及 Android NDK 以编译 JNI 模块。

## 许可证

MIT
