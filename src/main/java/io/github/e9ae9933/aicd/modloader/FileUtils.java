package io.github.e9ae9933.aicd.modloader;

import io.github.e9ae9933.aicd.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;

public interface FileUtils
{
	default void assertTrue(boolean b,String s)
	{
		if(!b)
			throw new RuntimeException(s);
	}
	default void copy(File src,File dest)
	{
		assertTrue(src.isFile(),src+" not file");
		dest.mkdirs();
		Utils.ignoreExceptions(()->Files.copy(src.toPath(),dest.toPath().resolve(src.getName()), StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.COPY_ATTRIBUTES));
		//Utils.ignoreExceptions(()->Runtime.getRuntime().exec(String.format("cmd /c copy /y \"%s\" \"%s\"", src.getAbsolutePath(),dest.getAbsolutePath())));

		/*
		try
		{
			FileInputStream fis=new FileInputStream(src);
			FileOutputStream fos = new FileOutputStream(new File(dest, src.getName()));
			int b;
			while((b=fis.read())!=-1)
				fos.write(b);
			fis.close();
			fos.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}*/
	}
	default void xcopy(File src,File dest)
	{
		//System.out.println("xcopy "+src+" "+dest);
		assertTrue(src.isDirectory(),src+" not directory");
		dest.mkdirs();
//		Utils.ignoreExceptions(()->Runtime.getRuntime().exec("cmd",new String[]{String.format("/c xcopy /e /i /y /l /h /r \"%s\" \"%s\"", src.getAbsolutePath(),dest.getAbsolutePath())}));
		Arrays.stream(src.listFiles()).parallel().forEach(f ->
		{
			if (f.isFile())
				copy(f, dest);
			else
				xcopy(f, new File(dest, f.getName()));
		});
	}
	default void rmdir(File dir)
	{
		if(!dir.isDirectory())
			return;
		Arrays.stream(dir.listFiles()).parallel()
				.forEach(f->{
					if(f.isFile())
						f.delete();
					else rmdir(f);
				});
		dir.delete();
	}
	default boolean hide(File dir)
	{
		try
		{
			Files.setAttribute(dir.toPath(), "dos:hidden", true);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	default String md5(byte[] b)
	{
		return Utils.ignoreExceptions(()->
		{
			StringBuilder sb=new StringBuilder();
			byte[] target=MessageDigest.getInstance("md5").digest(b);
			for(byte b1:target)
				sb.append(String.format("%02X", b1));
//			System.out.println("md5 "+file+sb);
			return sb.toString();
		});
	}
	default String md5(File file)
	{
		return Utils.ignoreExceptions(()->
		{
			byte[] b = MessageDigest.getInstance("md5").digest(Utils.readAllBytes(file));
			StringBuilder sb=new StringBuilder();
			for(byte b1:b)
				sb.append(String.format("%02X", b1));
//			System.out.println("md5 "+file+sb);
			return sb.toString();
		});
	}
	default String md5Dir(File dir)
	{
		if(!dir.isDirectory())
			throw new IllegalArgumentException("not dir");
		List<String> hash=new CopyOnWriteArrayList<>();
		hash.add("dir: "+dir.getName());
		Arrays.stream(dir.listFiles()).parallel()
				.forEach(f->{
					if(f.isFile())
						hash.add("file: "+f.getName()+" as "+md5(f));
					else hash.add("subdir: "+f.getName()+" as "+md5Dir(f));
				});
		StringJoiner stringJoiner=new StringJoiner("\0");
		hash.stream().sorted().forEach(s->stringJoiner.add(s));
		return md5(stringJoiner.toString().getBytes(StandardCharsets.UTF_8));
	}
}
