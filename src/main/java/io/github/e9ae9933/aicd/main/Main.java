package io.github.e9ae9933.aicd.main;

import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.client.Daemon;
import io.github.e9ae9933.aicd.modloader.L10n;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;

public class Main
{
	static int count=4;
	static String[] options=new String[]{
		"安装重定向工具","翻译初始化","图像初始化","（弃用）模组制作器"
	};
	static Consumer[] ops=new Consumer[]{
			args-> Utils.ignoreExceptions(()->InstallerMain.main((String[]) args)),
			args-> Utils.ignoreExceptions(()->L10nMain.main((String[]) args)),
			args-> Utils.ignoreExceptions(()->PxlsMain.main((String[]) args)),
			args-> Utils.ignoreExceptions(()-> io.github.e9ae9933.aicd.client.Main.main((String[]) args)),
	};
	public static void main(String[] args) throws Exception
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		OptionParser parser=new OptionParser();
		OptionSpecBuilder ikwid=parser.accepts("IKnowWhatImDoing");
		OptionSet set=parser.parse(args);

		if(!set.has(ikwid))
			checkAicIsHere();
		Daemon.createDaemon();
		int op=JOptionPane.showOptionDialog(null,"选择目标。","AIC 工具箱",JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,
				options,null);
		try
		{
			if(op<0||op>=count)
				System.exit(0);
			ops[op].accept(args);
		}
		catch (Throwable e)
		{
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.close();
			String str=sw.toString();
			JOptionPane.showMessageDialog(null, String.format("看起来任务遇到了一些错误。\n您可以加入群聊 907348590 与我们联系，或者尝试自己解决。\n一些可能有用的信息：\n%s", str),"任务失败",JOptionPane.ERROR_MESSAGE);
			System.exit(-2);
			return;
		}
		JOptionPane.showMessageDialog(null,"任务成功完成。","任务完成",JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}
	public static void checkAicIsHere()
	{
		if(!new File(("AliceInCradle.exe")).isFile())
		{
			JOptionPane.showMessageDialog(null,"路径不合法。\n请把工具放到和AliceInCradle.exe同目录下。","错误",JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		try
		{
			lock=new RandomAccessFile("session2.lock","rw");
			Objects.requireNonNull(lock.getChannel().tryLock());
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null,"另一个工具箱正在运行中。\n同一时间在同一个目录下只能运行一个。","错误",JOptionPane.ERROR_MESSAGE);
			System.exit(-2);
		}
	}
	private static RandomAccessFile lock;
}
