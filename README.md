# ClassConverter - IDEA 类型转换插件

ClassConverter 是一款强大的 IntelliJ IDEA 插件，用于简化 Java 类之间的对象转换过程。它可以自动完成字段映射和转换代码生成，支持单个对象和列表类型的转换。

## 主要特性

- 🔄 通过简单的 UI 界面实现 Java 类之间的转换
- 📝 智能字段名称匹配
- 📋 支持 List 类型的批量转换
- 🎯 智能变量命名建议
- 🔍 支持模糊匹配的高级类搜索
- 🗺️ 自定义字段映射配置
- ⚡ 实时类文件更新跟踪

## 使用方法

1. 在代码中选中要转换的变量
2. 按下快捷键 `Alt + Shift + C`（默认快捷键）或使用右键菜单
3. 使用类选择器选择目标类
4. 配置字段映射和转换选项
5. 点击确定生成转换代码

### 示例

```java
// 转换前
SourceDTO sourceDTO = getSourceDTO();

// 转换后（自动生成的代码）
TargetDTO targetDTO = new TargetDTO();
targetDTO.setId(sourceDTO.getId());
targetDTO.setName(sourceDTO.getName());
targetDTO.setDescription(sourceDTO.getDescription());
```

## 配置说明

### 单对象转换
- 可选择单对象转换或列表转换
- 自定义目标变量名
- 配置源类和目标类之间的字段映射

### 列表转换
- 自动处理 List 类型的转换
- 生成规范的遍历代码
- 保持类型安全

### 字段映射
- 交互式的字段映射界面
- 自动匹配相似字段名
- 支持不同字段名的自定义映射
- 可选择性包含/排除字段

## 安装方法

1. 打开 IntelliJ IDEA
2. 进入 `Settings/Preferences` → `Plugins`
3. 搜索 "ClassConverter"
4. 点击 `Install` 安装
5. 重启 IntelliJ IDEA

## 系统要求

- IntelliJ IDEA 2023.1 或更高版本
- Java 17 或更高版本

## 技术特点

1. 智能类搜索
   - 支持模糊匹配和精确匹配
   - 实时搜索结果更新
   - 按包名组织搜索结果

2. 字段映射
   - 自动识别同名字段
   - 支持自定义字段对应关系
   - 可视化映射配置界面

3. 代码生成
   - 自动生成标准的 Java 代码
   - 支持 getter/setter 方法调用
   - 保持代码格式和样式

## 常见问题

**Q: 如何修改默认快捷键？**
A: 在 IDEA 的 Settings → Keymap 中搜索 "Convert Class Fields" 进行修改

**Q: 支持哪些类型的转换？**
A: 支持所有 JavaBean 类型的转换，包括基本数据类型和它们的包装类，以及 List 类型的集合

**Q: 生成的代码是否支持自定义？**
A: 插件生成标准的 Java 代码，你可以根据需要自由修改生成的代码

## 参与贡献

欢迎提交 Issue 和 Pull Request 来帮助改进这个插件！

## 开源协议

本项目采用 MIT 协议开源 - 详见 [LICENSE](LICENSE) 文件