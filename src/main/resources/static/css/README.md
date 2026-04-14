# SportTrack UI 样式系统文档

## 色彩方案

### 主色调（印象派油画风）
- **珊瑚橙** `#FF7F50` - CTA按钮、强调元素
- **春绿** `#90EE90` - 成功状态、运动相关
- **天空蓝** `#87CEFA` - 信息提示、淡背景
- **金色** `#FFD700` - 徽章、高亮

### 渐变背景
- `page-login` - 日落渐变（暖橙色）
- `page-register` - 梦幻渐变（粉紫色）
- `page-activities` - 海洋渐变（蓝绿色）
- `page-challenges` - 自然渐变（绿色调）
- `page-profile` - 混合渐变（蓝绿黄）

## CSS类参考

### 布局
- `.container` - 主容器（最大宽度1200px）
- `.grid` / `.grid-2` / `.grid-3` / `.grid-4` - 网格布局
- `.flex` / `.flex-col` - Flex布局
- `.justify-between` / `.items-center` - 对齐工具

### 组件
- `.card` - 玻璃态卡片（半透明+模糊）
- `.btn` / `.btn-primary` / `.btn-secondary` / `.btn-info` / `.btn-warning` / `.btn-danger` / `.btn-outline` - 按钮
- `.form-control` - 输入框
- `.form-group` - 表单组
- `.table` / `.table-container` - 表格
- `.alert` / `.alert-success` / `.alert-danger` / `.alert-warning` / `.alert-info` - 警告框
- `.progress` / `.progress-bar` - 进度条
- `.badge` / `.badge-success` / `.badge-info` - 徽章

### 工具类
- `.text-center` / `.text-right` - 文本对齐
- `.mb-0` / `.mb-sm` / `.mb-md` / `.mb-lg` / `.mb-xl` - 底部间距
- `.mt-sm` / `.mt-md` / `.mt-lg` - 顶部间距
- `.p-sm` / `.p-md` / `.p-lg` / `.p-xl` - 内边距
- `.fade-in` - 淡入动画

## 页面背景类
在 `<body>` 标签上使用以下类：
- `page-login` - 登录页
- `page-register` - 注册页
- `page-activities` - 活动相关页面
- `page-challenges` - 挑战相关页面
- `page-profile` - 个人资料页面

## 响应式设计
- 桌面端：多列布局
- 平板（<768px）：单列布局，导航隐藏
- 手机（<480px）：全宽按钮，紧凑间距

## 使用示例

```html
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body class="page-activities">
    <div class="container">
        <div class="card">
            <h1>Titre</h1>
            <button class="btn btn-primary">Action</button>
        </div>
    </div>
</body>
</html>
```
