package io.github.e9ae9933.aicd.main;

import io.github.e9ae9933.aicd.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InstallerMain
{
	public static void main(String[] args) throws Exception
	{
		InputStream is= Utils.readFromResources("bepinex.zip",false);
		ZipInputStream zis=new ZipInputStream(is);
		ZipEntry entry=null;
		while((entry=zis.getNextEntry())!=null)
		{
			String name=entry.getName();
			System.out.println("target "+name);
			if(entry.isDirectory())
				new File(name).mkdirs();
			else {
				File parent=new File(name).getParentFile();
				if(parent!=null)parent.mkdirs();
				FileOutputStream fos=new FileOutputStream(name);
				int len;
				byte[] b=new byte[1024];
				while((len=zis.read(b))!=-1)
					fos.write(b,0,len);
				fos.close();
			}
		}
		zis.close();
		is.close();
		InputStream aut=Utils.readFromResources("AicUtils.dll",false);
		FileOutputStream fos=new FileOutputStream("BepInEx/plugins/AicUtils.dll");
		int len;
		byte[] b=new byte[1024];
		while((len=aut.read(b))!=-1)
			fos.write(b,0,len);
		fos.close();
		aut.close();
	}
}
