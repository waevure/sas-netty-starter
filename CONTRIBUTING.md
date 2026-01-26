# Contributing

感谢你愿意贡献 `sas-netty-starter`。为了让协作更顺畅，请遵循以下约定。

## 开发环境
- JDK 21
- Maven 3.9+

## 分支与提交
- 建议使用 feature 分支：`feature/xxx`
- 提交信息建议采用语义化描述，例如：`feat: add tcp handler`

## 构建与验证
```bash
mvn -DskipTests package
```

如新增功能，建议补充最小可跑 demo 或测试说明。

## 代码风格
- 保持现有包结构与命名风格
- 新增公共 API 时请更新 README / PROJECT_GUIDE

## Issue / PR 说明
- Bug 请提供：复现步骤、期望行为、实际行为、日志
- 新功能请说明：使用场景、接口设计、兼容性影响

## 授权
提交即表示你同意以项目当前 License 发布你的贡献。
