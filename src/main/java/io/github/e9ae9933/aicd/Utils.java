package io.github.e9ae9933.aicd;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Supplier;

public class Utils
{
	static File gamePath=null;
	public static void setGamePath(File gamePath)
	{
		Utils.gamePath=gamePath;
	}
	public static File getGamePath(boolean recalculate)
	{
		try
		{
			if (gamePath == null || recalculate)
			{
				if(false)
				throw new RuntimeException();
				String os = System.getProperty("os.name");
				String log;
				if (os.contains("Windows"))
//					log = "%APPDATA%\\..\\LocalLow\\NanameHacha\\AliceInCradle\\Player.log";
					log=System.getProperty("user.home")+"\\Appdata\\LocalLow\\NanameHacha\\AliceInCradle\\Player.log";
				else if (os.contains("Mac"))
					log = "~/Library/Application Support/jp.nonamehacha.AliceInCradle/Player.log";
				else
					log = null;
				FileReader fr=new FileReader(log);
				Scanner sc=new Scanner(fr);
				String s=sc.nextLine();
				sc.close();
				fr.close();
				String managed=s.substring(1+s.indexOf('\''),s.lastIndexOf('\''));
				File m=new File(managed);
				gamePath=m.getParentFile().getParentFile();
				System.out.println("Updated path to "+gamePath);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			gamePath= new File("");
			JOptionPane.showMessageDialog(null,"检测到的路径无效，请前往“设置”界面手动设置路径。","路径无效",JOptionPane.WARNING_MESSAGE);
		}
		return gamePath;
	}
	public static File getModPath()
	{
		File file= new File(getGamePath(false).getAbsolutePath() + "/BepInEx/plugins");
		if(file.exists()&&file.isDirectory())
			return file;
		throw new RuntimeException("路径不存在");
	}
	public static <T> T ignoreExceptions(SupplierWithExceptions<T> supplier)
	{
		try
		{
			return supplier.get();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	public static void ignoreExceptions(ConsumerWithExceptions supplier)
	{
		try
		{
			supplier.accept();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	@FunctionalInterface
	public interface SupplierWithExceptions<T>
	{
		public T get() throws Exception;
	}
	@FunctionalInterface
	public interface ConsumerWithExceptions
	{
		public void accept() throws Exception;
	}
}
