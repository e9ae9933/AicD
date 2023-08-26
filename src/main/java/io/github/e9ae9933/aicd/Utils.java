package io.github.e9ae9933.aicd;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Utils
{
	static File gamePath=null;
	public static void setGamePath(File gamePath)
	{
		Utils.gamePath=gamePath;
	}
	public static File getSavePath()
	{
		File file=new File(System.getProperty("user.home")+"\\Appdata\\LocalLow\\NanameHacha\\AliceInCradle");
		if(file.exists()&&file.isDirectory())
			return file;
		return null;
	}
	public static byte[] readAllBytes(InputStream is)
	{
		try
		{
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			BufferedInputStream bis=new BufferedInputStream(is);
			byte[] b=new byte[8192];
			int len;
			while((len=bis.read(b))>0)
				bos.write(b,0,len);
			return bos.toByteArray();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public static void writeAllUTFString(File file,String s)
	{
		ignoreExceptions(()->{
			FileOutputStream fos=new FileOutputStream(file);
			fos.write(s.getBytes(StandardCharsets.UTF_8));
			fos.close();
		});
	}
	public static void writeAllBytes(File file,byte[] b)
	{
		ignoreExceptions(()->{
			FileOutputStream fos=new FileOutputStream(file);
			fos.write(b);
			fos.close();
		});
	}
	public static String readAllUTFString(InputStream is)
	{
		return new String(readAllBytes(is), StandardCharsets.UTF_8);
	}
	public static String readAllUTFString(File file)
	{
		return new String(readAllBytes(file), StandardCharsets.UTF_8);
	}
	public static InputStream readFromResources(String name,boolean create)
	{
		try
		{
			File file = new File(name);
			if (!file.exists())
			{
				InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(name);
				if(is==null)
					throw new NullPointerException("File not found: "+name);
				if (create)
				{
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(readAllBytes(is));
					is.close();
					fos.close();
				}
				else
					return is;
			}
			return new FileInputStream(file);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public static byte[] readAllBytes(File file)
	{
		try
		{
			FileInputStream fis=new FileInputStream(file);
			byte[] b=readAllBytes(fis);
			fis.close();
			return b;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	public static File getGamePath(boolean recalculate)
	{
		try
		{
			if (recalculate)
			{
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
				if(!gamePath.exists())
					throw new RuntimeException(gamePath+" not exists");
				System.out.println("Updated path to "+gamePath);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			gamePath= null;
			JFileChooser chooser=new JFileChooser();
			chooser.setSize(800,600);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(false);
//			JOptionPane.showMessageDialog(null,"检测到的路径无效，请前往“设置”界面手动设置路径。","路径无效",JOptionPane.WARNING_MESSAGE);
			while(gamePath==null)
			{
				int id = JOptionPane.showConfirmDialog(null, "未检测到 AIC 路径。您想要现在指定游戏路径吗？\n如果您不知道这是什么，请点击“是”来指定路径。", "路径无效", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (id == JOptionPane.CLOSED_OPTION)
					System.exit(-1);
				if (id == JOptionPane.NO_OPTION)
					return gamePath;
				int chs = chooser.showOpenDialog(null);
				if (chs != JFileChooser.APPROVE_OPTION)
				{
					continue;
				}
				gamePath=chooser.getSelectedFile();
				File aic=new File(gamePath.getAbsolutePath()+"/AliceInCradle.exe");
				if(!aic.exists())
				{
					int ans=JOptionPane.showConfirmDialog(null,"该路径下未发现 AliceInCradle.exe。\n您确定您选择的是游戏可执行文件的父目录吗？\n如果你不确定，请点击“否”来重新选择。","路径怀疑",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
					if(ans!=JOptionPane.OK_OPTION)
					{
						gamePath=null;
						continue;
					}
				}
			}
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
			//e.printStackTrace();
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
			//e.printStackTrace();
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
