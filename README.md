<p align="center">
  <img src="docs/img/icon.png" alt="DroidPrivacy Icon" height="500" />
</p>

# DroidPrivacy

## 项目简介

DroidPrivacy 是一个基于 Xposed 框架的 Android 隐私检测工具，能够检测应用对设备隐私信息的获取行为。通过 Hook 系统 API，工具可以实时监控应用对设备信息、用户数据、权限使用等隐私相关操作的访问情况。

### 主要功能

- **隐私检测能力**：涵盖设备信息、用户数据、权限检查等全方位隐私检测
- **实时监控**：基于 Xposed Hook 技术，实时拦截和记录隐私 API 调用
- **详细日志**：提供完整的检测日志和结果文件，便于分析和报告
- **多应用支持**：支持同时检测多个应用的隐私行为
- **灵活配置**：可自定义检测规则和配置选项

### 适用场景

- **隐私合规检测**：帮助开发者检测应用的隐私合规性
- **安全研究**：用于 Android 应用安全分析和研究
- **隐私审计**：协助进行应用隐私行为审计和评估

## 快速开始

### 环境要求

- Android 8.1 至 Android 14 系统
- 已 Root 的设备或模拟器
- Magisk v24.0+ 和 LSPosed 框架

### 使用步骤

1. **环境配置**：按照[使用说明](docs/user_guide.md)完成环境配置
2. **安装工具**：下载并安装 DroidPrivacy APK
3. **配置检测**：在 LSPosed 中启用模块并选择目标应用
4. **开始检测**：配置检测规则并开始隐私检测
5. **查看结果**：获取检测日志和结果文件

### 详细文档

- **[使用说明](docs/user_guide.md)** - 完整的使用流程指南
- **[检测规则说明](docs/detection_rules.md)** - 检测规则详细说明
- **[常见问题解答](docs/faq.md)** - 使用过程中的常见问题及解答
- **[问题反馈指南](docs/feedback.md)** - 问题反馈和 Issue 提交指导

## 更新说明

查看项目的版本更新记录和功能变更：

- **[CHANGELOG](CHANGELOG.md)** - 详细的版本更新日志

## 致谢

本项目基于以下开源项目构建，特此致谢：

### 核心依赖

- **[XposedBridge](https://github.com/rovo89/XposedBridge)** - 提供 Xposed 框架的核心 API，是本项目的基础技术栈
- **[LSPosed](https://github.com/LSPosed/LSPosed)** - 现代化的 Xposed 框架实现，提供稳定可靠的 Hook 能力
- **[rootAVD](https://github.com/newbit1/rootAVD)** - Android 模拟器 Root 工具，为测试环境提供便利

### 相关项目

- **[Riru](https://github.com/RikkaApps/Riru)** - LSPosed 的传统依赖，虽然已停止维护，但在早期版本中发挥了重要作用
- **[Magisk](https://github.com/topjohnwu/Magisk)** - 系统级 Root 解决方案，为 LSPosed 提供运行环境

感谢这些优秀的开源项目为 Android 生态做出的贡献，使得 DroidPrivacy 能够实现其功能目标。

## 贡献指南

我们欢迎所有形式的贡献！无论是代码、文档、问题反馈还是功能建议，都能帮助项目变得更好。

### 贡献方式

#### 1. 问题反馈
- **Bug报告**：发现功能异常或崩溃问题
- **使用问题**：环境配置或使用方法咨询
- **功能建议**：新功能需求或现有功能改进建议

请使用[问题反馈指南](docs/feedback.md)中的标准模板提交Issue。

#### 2. 代码贡献
- **Bug修复**：修复已知问题
- **功能开发**：实现新功能或改进现有功能
- **代码优化**：性能优化、代码重构等
- **测试用例**：添加或完善测试代码

#### 3. 文档改进
- **使用文档**：完善使用说明和FAQ
- **代码注释**：改进代码注释和文档
- **示例代码**：提供使用示例和最佳实践

### 贡献流程

1. **Fork项目**：Fork本仓库到您的GitHub账户
2. **创建分支**：创建新的功能分支 (`git checkout -b feature/AmazingFeature`)
3. **提交更改**：提交您的更改 (`git commit -m 'Add some AmazingFeature'`)
4. **推送分支**：推送到您的分支 (`git push origin feature/AmazingFeature`)
5. **创建PR**：在GitHub上创建Pull Request

### 代码规范

- 遵循项目的代码风格和命名规范
- 提交信息请遵循[提交信息规范](docs/commit_convention.md)
- 确保代码通过所有测试
- 添加必要的注释和文档

## 许可证

本项目采用 [Apache 2.0 许可证](LICENSE)。
