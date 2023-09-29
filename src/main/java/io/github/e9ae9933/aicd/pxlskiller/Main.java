package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Utils;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

class Test
{
	public static void main(String[] args) throws Exception
	{
		Settings s = new Settings();
		s.externalResourcesDir = new File("F:\\work\\origin\\Texture2D");
		s.pxlsName = "noel.pxls";
		InputStream is = Utils.readFromResources("noel.pxls", false);
		NoelByteBuffer buf = new NoelByteBuffer(Utils.readAllBytes(is));
		is.close();
		PxlCharacter chara = new PxlCharacter(buf, s);
		//System.out.println(s.referenceMap);
		chara.export(new File("tryexport"), s);

		Settings set = new Settings();
		PxlCharacter character = new PxlCharacter(new File("tryexport"), set);
		FileOutputStream fos = new FileOutputStream("test.pxls");
		fos.write(character.outputAsBytes());
		fos.close();

	}
}

public class Main
{
	public static void main(String[] args) throws Exception
	{
		long time = System.currentTimeMillis();
		System.out.println("Process start with args " + args.length + " " + Arrays.toString(args));
		OptionParser optionParser = new OptionParser();
		ArgumentAcceptingOptionSpec<String> output = optionParser.accepts("output").withRequiredArg();
		ArgumentAcceptingOptionSpec<String> dir = optionParser.accepts("dir").withRequiredArg();
		ArgumentAcceptingOptionSpec<String> textureDir = optionParser.accepts("textureDir").withRequiredArg();
		OptionSpec<Void> delete = optionParser.accepts("delete");
		OptionSpec<Void> noExtra = optionParser.accepts("noExtra");
		OptionSet optionSet = optionParser.parse(args);
		File outputDir = new File(optionSet.valueOf(output)/*u*/);
		File pxlsDir = new File(optionSet.valueOf(dir));
		File resourcesDir = new File(optionSet.valueOf(textureDir));
		boolean shouldDelete = optionSet.has(delete);
		boolean writeExtra = optionSet.has(noExtra) == false;
		if (outputDir.isDirectory() && pxlsDir.isDirectory() && resourcesDir.isDirectory())
		{
			int n = Runtime.getRuntime().availableProcessors();
//			ExecutorService service= Executors.newFixedThreadPool(n);
			System.out.println("Launch with " + n + " thread(s)");
			//service.execute();
			Arrays.stream(pxlsDir.listFiles()).collect(Collectors.toList())
					.parallelStream().forEach(file->
			{
				System.out.println((System.currentTimeMillis() - time) + " " + Thread.currentThread().getName() + " started with file " + file.getPath());
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
					s.writeExtra = writeExtra;
					PxlCharacter chara = new PxlCharacter(buf, s);
					File to = new File(outputDir, file.getName().substring(0, file.getName().lastIndexOf(".")));
					//System.out.println("output into " + to.getAbsolutePath());
					to.mkdirs();
					chara.export(to, s);
					System.out.println("Exported " + file.getName());
					if (shouldDelete)
						file.delete();
				} catch (Exception e)
				{
					System.out.println("Export " + file.getName() + " failed with " + Arrays.toString(e.getStackTrace()));
					throw new RuntimeException(e);
				}
				System.out.println((System.currentTimeMillis() - time) + " " + Thread.currentThread().getName() + " ended with file " + file.getPath());

			});
//			service.shutdown();
//			while(!service.isTerminated())Thread.sleep(1);
//			System.out.println("Service terminated");
			System.out.println("Time used " + (System.currentTimeMillis() - time) + " ms");
		} else throw new IllegalArgumentException("Some args are not directory or just one arg is not directory");
	}
}
