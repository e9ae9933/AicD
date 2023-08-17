package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Utils;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		long time=System.currentTimeMillis();
		System.out.println("Process start with args "+args.length+" "+Arrays.toString(args));
		OptionParser optionParser=new OptionParser();
		ArgumentAcceptingOptionSpec<String> output=optionParser.accepts("output").withRequiredArg();
		ArgumentAcceptingOptionSpec<String> dir=optionParser.accepts("dir").withRequiredArg();
		ArgumentAcceptingOptionSpec<String> textureDir=optionParser.accepts("textureDir").withRequiredArg();
		OptionSpec<Void> delete=optionParser.accepts("delete");
		OptionSet optionSet=optionParser.parse(args);
		File outputDir=new File(optionSet.valueOf(output)/*u*/);
		File pxlsDir=new File(optionSet.valueOf(dir));
		File resourcesDir=new File(optionSet.valueOf(textureDir));
		boolean shouldDelete=optionSet.has(delete);
		if(outputDir.isDirectory()&&pxlsDir.isDirectory()&&resourcesDir.isDirectory())
		{
			int n=Runtime.getRuntime().availableProcessors();
			ExecutorService service= Executors.newFixedThreadPool(n);
			System.out.println("Launch with "+n+" thread(s)");
			//service.execute();
			for(File file:pxlsDir.listFiles())
			{
				service.execute(() ->
				{
					System.out.println((System.currentTimeMillis()-time)+" "+Thread.currentThread().getName()+" started with file "+file.getPath());
					try
					{
						if (file.isDirectory() || !file.getName().endsWith(".pxls"))
						{
							//System.out.println("Found something seems like it is not a pxls: " + file.getName());
							return;
						}
						FileInputStream fis = new FileInputStream(file);
						byte[] b = Utils.readAllBytes(fis);
						fis.close();
						System.out.println("read " + file.getName() + " with length " + b.length);
						NoelByteBuffer buf = new NoelByteBuffer(b);
						Settings s = new Settings();
						s.shouldDelete = shouldDelete;
						s.externalResourcesDir = resourcesDir;
						s.pxlsName = file.getName();
						PxlCharacter chara = new PxlCharacter(buf, s);
						File to = new File(outputDir, file.getName().substring(0, file.getName().lastIndexOf(".")));
						//System.out.println("output into " + to.getAbsolutePath());
						to.mkdirs();
						chara.export(to, s);
						System.out.println("Exported " + file.getName());
					}
					catch (Exception e)
					{
						System.out.println("Export "+file.getName()+" failed with "+ Arrays.toString(e.getStackTrace()));
						e.printStackTrace();
					}
					System.out.println((System.currentTimeMillis()-time)+" "+Thread.currentThread().getName()+" ended with file "+file.getPath());
				});
			}
			service.shutdown();
			while(!service.isTerminated())Thread.sleep(1);
			System.out.println("Service terminated");
			System.out.println("Time used "+(System.currentTimeMillis()-time)+" ms");
		}
		else throw new IllegalArgumentException("Some args are not directory or just one arg is not directory");
	}
}
