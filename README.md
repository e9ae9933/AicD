# AicD
# AliceInCradle 工具箱 (AicD of "java edition") 说明

_e9ae9933_, 2023/6/7

version 1.2

# 目前没有完全支持mac的计划。

**因为我们并没有使用mac的开发成员，所以虽然程序可能可以在mac上运行，但是它不一定会正常工作。**

**如果您拥有一定的开发能力或使用mac的能力，可以联系我们。**

## 说明

​		一个简单的小工具。

```
Killer from the world of man
Only you understand
We're pitiless killers both
So just follow my plan
```

​		本工具的开发目标是实现一键安装模组的功能。

功能列表:

1. 下载/更新器
2. 存档编辑器
3. PXLS分解器

## 在这之前

$\color{red}{\bold{你需要安装Java8或以上的Java运行库。}}$

你可以访问这个网址来下载$Java17$：https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.exe

如果您想选择安装的版本，有更多个性化的选择，请访问

https://www.oracle.com/java/technologies/downloads/#jdk17-windows

## 设置

​		**您首先应当在`设置`中确认工具检测到了正确的`AIC`路径。**

​		一般来说，正确的路径下应当直接包含`AliceInCradle.exe`。

​		如果您发现路径不正确或者未检测到路径，请在路径输入框中输入对应的路径或者点击右边的`浏览`按钮选择路径。

## 下载

​		提供`Nepkey纳百技`的`AicUpdater`功能的一部分。

​		该功能用于自动下载更新`AIC`本体。

​		请在左侧选择目标版本，然后点击`运行`，程序将自动下载并解压对应版本。

​		**注意：部分镜像源下载速度较慢，如果您认为下载速度过慢，可以尝试其它源。**

​		如果您发现版本列表未被及时更新，请联系我们。

`保留 _debug.txt`: 字面意思，解压时略过调试设置。

`重新下载`: 简而言之，取消勾选可以避免重复下载。如果出错可以试着勾选。

## 模组

安装模组功能被移动到`模组开发工具箱`。

### 存档编辑

​		简单的存档编辑器。

​		存档编辑器只能编辑特定版本的存档。一般来说，最新版本的`AIC`存档会被支持。

​		**如果你发现自己的存档无法被读取，请先确认这个存档被最新的AIC游戏保存后，再与我们联系。**

​		点击`浏览`来打开一个文件。

## 技术信息

### 启动参数

​		**如果你不知道这是什么，可以略过，不影响使用。**

​				`--integrated`: 启动内置服务端。在服务器出错时可以使用来运行内置服务端。

​				`--server <host:port>`: 手动指定服务器地址。与`--integrated`冲突。

### 内置服务端

主类: `io.github.e9ae9933.aicd.server.Main`

以专用服务端的形式启动。无图形界面。

| 参数名称         | 值类型       | 参数意义              | 默认      |
|--------------|-----------|-------------------|---------|
| `port`       | `int`     | 指定服务端监听的端口        | `10051` |
| `integrated` | `boolean` | 指定服务端是否以内置服务端形式启动 | `false` |

### PxlsKiller

主类: `io.github.e9ae9933.aicd.pxlskiller.Main`

启动 PXLS 分解器。将一个目录下的 pxls 文件分解为许多 png 文件和一些 json 描述文件。

| 参数名称         | 值类型       | 参数意义               | 默认      |
|--------------|-----------|--------------------|---------|
| `output`     | `String`  | 指定输出文件路径           | 必要      |
| `dir`        | `String`  | 指定要分解的 pxls 文件路径   | 必要      |
| `textureDir` | `String`  | 指定外置于 pxls 文件的图像路径 | 必要      |
| `delete`     | `boolean` | 指定是否在读取图像后删除图像     | `false` |



# 特别鸣谢

下列是不完全的对程序开发有帮助或提出了建议的玩家名单。

```
Cicini as "Shiro"
e9ae9933 as "Team NyaPaint"
废线妖精 as "武装直升机"
Helhest as "Nttodosth"
hinayua_r18&Hashino Mizuha as "NanameHacha"
Nepkey纳百技
诺艾尔Noel as "NoelCornehl"
凌空的猫 as "Flying Cat" as "Feather Feline"
普莉姆拉老师
小安awa
一键山雏 as "键山雏一"
YouTheB
```

# 开放源代码许可

```
org.junit.jupiter:junit-jupiter-api Eclipse Public License v2.0
org.junit.jupiter:junit-jupiter-engine Eclipse Public License v2.0
com.google.code.gson:gson Apache License 2.0
net.sf.jopt-simple:jopt-simple MIT License
org.snakeyaml:snakeyaml-engine Apache License 2.0
```

