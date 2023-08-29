package io.github.e9ae9933.aicd.modloader;

public enum L10n
{
	NO_AIC_DIR("未找到缓存的 AliceInCradle 路径。\n将要请求指定路径。"),NO_AIC_DIR_TITLE("未找到路径"),
	FOUND_AIC_DIR("找到 AliceInCradle 路径：\n%s\n确定要使用这个路径吗？\n建议使用一个新的 AliceInCradle 安装。"),FOUND_AIC_DIR_TITLE("已找到路径"),
	FOUND_GIT("在环境变量中找到 Git，返回值 %d，版本为\n%s\n将使用该 Git。"),FOUND_GIT_TITLE("找到 Git"),
	NO_GIT("未在环境变量中找到 Git。\n将尝试自动从 https://mirrors.tuna.tsinghua.edu.cn/ 获取 PortableGit。\n删除时无需卸载。"),NO_GIT_TITLE("未找到 Git"),
	DL_GIT("找到 Git 版本 %s。\n将下载：\n%s\n至当前路径。"),DL_GIT_TITLE("即将下载 Git"),
	DLING("正在连接"),DLING_TITLE("正在下载 %s"),
	FOUND_PGIT("在 AliceInCradle 路径中找到 PortableGit，返回值 %d，版本为\n%s\n将使用该 Git。"),FOUND_PGIT_TITLE("找到 PortableGit"),
	BEX("未找到有效 BepInEx。\n需要为您自动安装吗？\n建议使用一个新的 AliceInCradle 安装，工具目前不提供卸载功能。"),BEX_TITLE("未找到 BepInEx"),
	AICUTILS("未找到有效 AicUtils。\n需要为您自动安装吗？"),AICUTILS_TITLE("未找到 AicUtils"),
	UNPACK("将要进行必要操作：自动解包。\n这可能需要几分钟，请不要关闭即将弹出的 AliceInCradle 窗口。"),UNPACK_TITLE("未找到有效解包"),
	REDIRECT("将要进行必要操作：资源处理。\n这可能需要几分钟甚至几十分钟！\n请耐心等待……"),REDIRECT_TITLE("未找到有效资源"),
	MAINTITLE("模组选择"),
	INITIALIZING("正在初始化……"),
	NO_AUTHOR("无作者信息"),
	BUILD("构建模组"),
	OPEN_DIR("打开目录"),
	NO_PACK_PNG("<html>无法读取<br>pack.png</html>"),
	BORDER_WORK_MODS("开发路径下的模组"),
	UNKNOWN_VERSION("未知版本"),
	CREATE_NEW_MOD("创建新模组"),
	CREATING_MOD("正在创建新模组……"),
	INPUT_MOD_NAME("请输入模组名称"),INPUT_MOD_NAME_TITLE("创建新模组"),
	FALSE_MOD_PATH("与已有模组冲突或不是合法路径 (请输入大小写字母、字符或下划线)"),
	NO_WORKING("未找到工作路径下的开发中模组。\n试试创建新模组？"),
	FAILED_READING("无法读取或不存在 info.json (可能不是模组目录)"),
	WORKING("<html>正在创建新模组 %s。<br>这可能需要十分钟甚至九分钟，且可能花费数十分钟。<br>您可以去做一些其它的事情。</html>"),
	INIT_MOD_FINISH("已完成对模组 %s 的创建。\n用时 %s。\n是否打开模组文件夹？"),INIT_MOD_FINISH_TITLE("创建完成"),
	BUILDING_MOD("<html>正在构建模组 %s<br>这通常来说会比较快。<br>详细信息请查看日志。<br>懒得做进度条。</html>"),
	BUILD_MOD_FINISH("已完成对模组 %s 的构建。\n是否打开导出文件夹？"),BUILD_MOD_FINISH_TITLE("构建完成"),
	BUILD_MOD_FAILED("构建失败，请查看日志。"),
	INIT_MOD_FAILED("创建失败，请查看日志。"),
	RUNNING_AIC("<html>正在运行 AliceInCradle。<br>不过在那之前，需要先处理一些模组的事项。<br>下面是可能的输出，也可能没有。</html>"),
	RUN_AIC("运行 AIC 程序")
	;
	String cn;
	L10n(String cn)
	{
		this.cn=cn;
	}
	public String toString()
	{
		return cn;
	}
}
