# PineappleTV - Android TV 视频播放应用

基于Android TV平台开发的本地视频播放应用，支持自动扫描、媒体库管理和播放历史记录。

## 技术栈

- **UI框架**: Jetpack Compose + Compose for TV
- **编程语言**: Kotlin
- **数据库**: SQLDelight
- **依赖注入**: Koin
- **视频播放**: ExoPlayer
- **图片加载**: Coil
- **导航**: Navigation Compose

## 主要功能

### 1. 首次启动引导
- 选择视频文件夹
- 自动扫描并创建媒体库索引
- 支持多种视频格式（mp4, avi, mkv, mov, wmv, flv, webm, m4v, 3gp, ts, m2ts等）

### 2. 媒体库管理
- 按文件夹自动分组为合集
- 显示合集封面和名称
- 支持视频缩略图生成

### 3. 主界面功能
- 展示所有视频合集
- 最近播放列表
- 记录播放进度
- 一键搜索功能

### 4. 视频播放
- 支持多种视频格式播放
- 自动记录播放位置
- 支持播放列表
- 自动播放下一集功能
- 播放完成后可选择回退到列表

### 5. 搜索功能
- 按视频文件名搜索
- 显示搜索结果的封面和基本信息
- 支持模糊匹配

## 项目结构

```
app/src/main/java/app/pineappletv/
├── data/
│   ├── database/          # 数据库管理
│   ├── model/            # 数据模型
│   ├── repository/       # 数据仓库层
│   └── scanner/          # 视频文件扫描
├── di/                   # 依赖注入配置
├── ui/
│   ├── navigation/       # 导航管理
│   ├── screen/          # Compose界面
│   ├── theme/           # 主题配置
│   └── viewmodel/       # ViewModel层
├── MainActivity.kt      # 主Activity
└── PineappleTVApplication.kt  # 应用类
```

## 数据库设计

### Collections（合集表）
- id: 主键
- name: 合集名称
- path: 文件夹路径
- cover_image: 封面图片路径
- created_at/updated_at: 时间戳

### Videos（视频表）
- id: 主键
- collection_id: 所属合集ID
- name: 视频名称
- file_path: 文件路径
- cover_image: 封面图片
- duration: 视频时长
- file_size: 文件大小

### PlaybackHistory（播放历史表）
- id: 主键
- video_id: 视频ID
- position: 播放位置
- duration: 总时长
- last_played_at: 最后播放时间

## 权限要求

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 构建和运行

1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 确保Android TV模拟器或真机已连接
4. 运行项目

```bash
./gradlew assembleDebug
```

## 使用说明

1. **首次启动**: 选择包含视频文件的文件夹，应用会自动扫描并建立索引
2. **浏览媒体库**: 在主界面查看所有合集，点击进入查看合集内的视频
3. **播放视频**: 点击视频开始播放，支持进度记录和自动播放下一集
4. **搜索视频**: 使用搜索功能快速找到想要的视频
5. **继续播放**: 从最近播放列表继续观看未完成的视频

## 特色功能

- ✅ 完全基于Compose构建的现代化UI
- ✅ 支持Android TV遥控器操作
- ✅ 自动视频文件扫描和分类
- ✅ 播放进度自动保存
- ✅ 支持自动播放下一集
- ✅ 响应式设计，适配不同屏幕尺寸
- ✅ 本地数据库存储，无需网络连接

## 后续计划

- [ ] 支持网络视频流
- [ ] 添加字幕支持
- [ ] 视频封面自动生成
- [ ] 支持更多视频格式
- [ ] 添加播放列表管理
- [ ] 支持多语言