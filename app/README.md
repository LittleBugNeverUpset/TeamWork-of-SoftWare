# MiNote源码分析报告


> **小组成员：**
> **组长**：张铭昊
> **组员**：张艺博，王以利，杨站，种昊阳

## 前言

本项目为小米便签开源项目软件工程源码阅读分析报告，报告将从以下方面进行：

1. 对项目整体进行预览，分析各个模块的功能。
2. 使用代码质量检测工具SonarLint对代码质量进行检测并对其进行优化
3. 尝试开发新功能


## app文件夹初步分析

项目原码的项目结构如下:
``` bash
├─java                                          #存放应用中所有java源代码
│  └─net                                                
│      └─micode
│          └─notes                              #net.micode.notes遵循Maven工程中GroupID 的命名格式
│              ├─data                           #minote数据库相关的操作，如初始化与CURD
│              ├─gtask
│              │  ├─data                        #数据库操作
│              │  ├─exception                   #异常捕获
│              │  └─remote                      #远端任务相关
│              ├─model                          #标签项功能
│              ├─tool                           #数据处理、元素解析、备份等工具类
│              ├─ui                             #图形界面相关
│              └─widget                         #桌面挂件大小设置
└─res
    ├─color
    ├─drawable
    ├─drawable-hdpi
    ├─layout
    ├─layout-sw600dp
    ├─menu
    ├─mipmap-anydpi-v26
    ├─mipmap-hdpi
    ├─mipmap-mdpi
    ├─mipmap-xhdpi
    ├─mipmap-xxhdpi
    ├─mipmap-xxxhdpi
    ├─raw
    ├─raw-zh-rCN
    ├─values
    ├─values-night
    ├─values-zh-rCN
    ├─values-zh-rTW
    └─xml
```
最主要可以分为以下两类：

1. java：存放应用中所有java源代码
   - 包：
    Java文件夹有一个子文件夹net.micode.notes，子文件夹中包含如下包：
    **data**、**gtask**、**model**、**tool**、**ui**、**widge**
    包将有联系的、方向大致相同的模块组织在一起，简单说就是一个文件夹。java中引入包的主要原因是java本身跨平台特性的需求。java中的所有的资源都是以文件方式组织，这其中主要包含大量的类文件需要组织管理。java中采用目录树形结构，使用"."来分隔目录，这就是为什么会出现“**net.micode.notes**”这种包名。
    由于我们最近学习了和java开发相关的一些知识，得知这种命名规范是来自于Maven工程构建中遵循的**gvap**属性。Maven 中的`GAVP`是指`GroupId、ArtifactId、Version、Packaging`等四个属性的缩写，其中前三个是必要的，而 Packaging 属性为可选项。这四个属性主要为每个项目在maven仓库总做一个标识，类似人的 **《姓-名》** 。有了具体标识，方便maven软件对项目进行管理和互相引用！
    **GroupID 格式**：`com.{公司/BU }.业务线.[子业务线]`，最多 4 级。
        - 说明：{公司/BU} 例如：`alibaba/taobao/tmall/aliexpress` 等 BU 一级；子业务线可选。
        - 正例：`com.taobao.tddl` 或 `com.alibaba.sourcing.multilang  net.micode.notes`
    - 类
    打开包文件夹，我们会发现下面出现了许多文件，那些就是类。
    类是一个描述对象行为和状态的模板，对象是类的一个实例。类的声明中通常包含类的属性（数据）和类的方法（函数）。
    我们打开类文件，看到第一句代码是package net.micode.notes.“包名”。这是一个包的声明，它必须在第一行，以防止不同包中同名类之间发生冲突。通常一个包里存放相关的、功能相似的类。同一个包中的类相互访问，不用指定包名。

2. res：存放应用中构建UI的XML文件或者一些图片，字体设置，String映射等资源文件
   这部分**不在我们小组的源码阅读计划的重点中**

``` bash
# app.main.java文件夹目录文件具体如下：
.
└── net
    └── micode
        └── notes
            ├── data
            │   ├── Contact.java                                #联系人数据库
            │   ├── NotesDatabaseHelper.java                    #便签数据库，用于记录便签相关属性和数据
            │   ├── Notes.java                                  #便签信息提供类
            │   └── NotesProvider.java                          #数据库帮助类，用于辅助创建、处理数据库的条目
            ├── gtask
            │   ├── data
            │   │   ├── MetaData.java                           #关于同步任务的元数据
            │   │   ├── Node.java                               #同步任务的管理结点，用于设置、保存同步动作的信息
            │   │   ├── SqlData.java                            #数据库中基本数据，方法包括读取数据、获取数据库中数据、提交数据到数据库
            │   │   ├── SqlNote.java                            #数据库中便签数据，方法包括读取便签内容、从数据库中获取便签数据、设置便签内容、提交便签数据到数据库
            │   │   ├── Task.java                               #同步任务，将创建、更新和同步动作包装成JSON对象，用本地和远程的JSON对结点内容进行设置，获取同步信息，进行本地和远程的同步
            │   │   └── TaskList.java                           #同步任务列表，将Task组织成同步任务列表进行管理
            │   ├── exception
            │   │   ├── ActionFailureException.java             #动作失败异常
            │   │   └── NetworkFailureException.java            #网络异常失败
            │   └── remote
            │       ├── GTaskASyncTask.java                     #GTask异步任务，方法包括任务同步和取消，显示同步任务的进程、通知和结果
            │       ├── GTaskClient.java                        #GTask客户端，提供登录Google账户，创建任务和任务列表，添加和删除结点，提交、重置更新、获取任务列表等功能
            │       ├── GTaskManager.java                       #GTask管理者，提供同步本地和远端的任务，初始化任务列表，同步内容、文件夹，添加、更新本地和远端结点，刷新本地同步任务ID等功能
            │       └── GTaskSyncService.java                   #GTask同步服务，用于提供同步服务 （开始、取消同步），发送广播
            ├── MainActivity.java
            ├── model
            │   ├── Note.java                                   #单个便签项
            │   └── WorkingNote.java                            #当前活动便签项
            ├── tool
            │   ├── BackupUtils.java                            #备份工具类，用于数据备份读取、显示
            │   ├── DataUtils.java                              #便签数据处理工具类，封装如查找、移动、删除数据等操作
            │   ├── GTaskStringUtils.java                       #同步中使用的字符串工具类，为了jsonObject提供string对象
            │   └── ResourceParser.java                         #界面元素的解析工具类，利用R.java这个类获取资源供程序调用
            ├── ui
            │   ├── AlarmAlertActivity.java                     #闹铃提醒界面
            │   ├── AlarmInitReceiver.java                      #闹铃提醒启动消息接收器
            │   ├── AlarmReceiver.java                          #闹铃提醒接收器
            │   ├── DateTimePickerDialog.java                   #设置提醒时间的对话框界面
            │   ├── DateTimePicker.java                         #设置提醒时间的部件
            │   ├── DropdownMenu.java                           #下拉菜单界面
            │   ├── EditDialog.java                             #
            │   ├── FoldersListAdapter.java                     #文件夹列表链接器（链接数据库）
            │   ├── NoteEditActivity.java                       #便签编辑活动
            │   ├── NoteEditText.java                           #便签的文本编辑界面
            │   ├── NoteItemData.java                           #便签项数据
            │   ├── NotesListActivity.java                      #主界面，用于实现处理文件夹列表的活动
            │   ├── NotesListAdapter.java                       #便签列表链接器（链接数据库）
            │   ├── NotesListItem.java                          #TODO
            │   └── NotesPreferenceActivity.java                #便签同步的设置界面
            └── widget
                ├── NoteWidgetProvider_2x.java
                ├── NoteWidgetProvider_4x.java
                └── NoteWidgetProvider.java

12 directories, 41 files

```
以上是我们小组对于原码整体框架结构的初步了解，接下来我们将分模块对源码进行分析、测试、优化以及总结


## 具体源码阅读分析见下方链接

### data文件夹

``` bash 
data文件夹目录结构如下：

.
├── Contact.java
├── NotesDatabaseHelper.java
├── Notes.java
└── NotesProvider.java

0 directories, 4 files

```
[data包源码分析](./src/main/java/net/micode/notes/data/README.md)


### tool文件夹
``` bash
.
├── BackupUtils.java
├── DataUtils.java
├── GTaskStringUtils.java
└── ResourceParser.java

0 directories, 4 files
```
[tool包源码分析](./src/main/java/net/micode/notes/tool/README.md)
