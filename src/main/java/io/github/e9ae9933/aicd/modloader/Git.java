package io.github.e9ae9933.aicd.modloader;

import io.github.e9ae9933.aicd.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Git
{
	public static File targetGit=null;
	File dir;
	List<String> def=new ArrayList<>();
	Git() {
		Arrays.stream(new String[]{
				"-c", "core.filemode=false",
				"-c","user.name=AliceInCradle toolbox",
				"-c","user.email=null@aictoolbox.top"
//				"-c", "core.autocrlf=false",
//				"-c", "core.safecrlf=true"
		}).forEachOrdered(s->def.add(s));
	}
	Git(File dir)
	{
		this();
		this.dir=dir;
	}
//	int call(OutputStream redirectStdout,OutputStream RedirectStderr String... args)
	int call(String... args)
	{
		return Utils.ignoreExceptions(()->
		{
			long time=System.currentTimeMillis();
			ProcessBuilder processBuilder = new ProcessBuilder();
			processBuilder.directory(dir);
//			processBuilder.inheritIO();
			List<String> cmd = new ArrayList<>();
			if(targetGit==null)
				cmd.add("git");
			else cmd.add(targetGit.getAbsolutePath());
			cmd.addAll(def);
			Arrays.stream(args).forEachOrdered(a -> cmd.add(a));
			processBuilder.command(cmd);
			System.out.println("running "+ processBuilder.command());
			Process p = processBuilder.start();
			Thread out=new Thread(()->{try{
					InputStream is=p.getInputStream();
					int b;
					while((b=is.read())!=-1)
					{
						System.out.write(b);
						if(b=='\r'||b=='\n')
							System.out.flush();
					}
					System.out.println("out end");
				}catch (Exception e){e.printStackTrace();}
			});
			Thread err=new Thread(()->{try{
				InputStream is=p.getErrorStream();
				int b;
				while((b=is.read())!=-1)
				{
					System.err.write(b);
					if(b=='\r'||b=='\n')
						System.err.flush();
				}
				System.err.println("err end");
			}catch (Exception e){e.printStackTrace();}
			});
			out.start();
			err.start();

			Thread killer=new Thread(()->{
				p.destroy();
			});
			Runtime.getRuntime().addShutdownHook(killer);
			int rt=p.waitFor();
			Runtime.getRuntime().removeShutdownHook(killer);
			System.out.println("returned "+rt+" time used "+(System.currentTimeMillis()-time)+" ms");
			return rt;
		});
	}
}
