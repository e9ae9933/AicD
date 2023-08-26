package io.github.e9ae9933.aicd.modloader;

import io.github.e9ae9933.aicd.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Git
{
	File dir;
	List<String> def=new ArrayList<>();
	Git() {
		Arrays.stream(new String[]{
				"-c", "core.filemode=false",
				"-c", "core.autocrlf=false",
				"-c", "core.safecrlf=true"
		}).forEachOrdered(s->def.add(s));
	}
	Git(File dir)
	{
		this();
		this.dir=dir;
	}
//	int call(OutputStream redirectStdout,OutputStream RedirectStderr String... args)
	byte[] call(String... args)
	{
		return Utils.ignoreExceptions(()->
		{
			long time=System.currentTimeMillis();
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.directory(dir);
			List<String> cmd = new ArrayList<>();
			cmd.add("git");
			cmd.addAll(def);
			Arrays.stream(args).forEachOrdered(a -> cmd.add(a));
			processBuilder.command(cmd);
			System.out.println("running "+ processBuilder.command());
			Process p = processBuilder.start();
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			Thread stdout=new Thread(()->{
				try
				{
					InputStream is=p.getInputStream();
					byte[] buf=new byte[8192];
					int len;
					while((len=is.read(buf))!=-1)
					{
						System.out.write(buf,0,len);
						baos.write(buf,0,len);
					}
					System.out.println("thread stopped");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			stdout.start();
			Thread stderr=new Thread(()->{
				try
				{
					InputStream is=p.getErrorStream();
					byte[] buf=new byte[8192];
					int len;
					while((len=is.read(buf))!=-1)
					{
						System.err.write(buf,0,len);
//						baos.write(buf,0,len);
					}
					System.out.println("thread stopped");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
			stderr.start();
			Thread killer=new Thread(()->{
				p.destroy();
				System.out.println("destroyed thread "+p);
			});
			Runtime.getRuntime().addShutdownHook(killer);

			int rt=p.waitFor();
			while(stdout.isAlive()||stderr.isAlive())Thread.yield();
			System.out.println("returned "+rt+" time used "+(System.currentTimeMillis()-time)+" ms");
			Runtime.getRuntime().removeShutdownHook(killer);
			return baos.toByteArray();
		});
	}
}
