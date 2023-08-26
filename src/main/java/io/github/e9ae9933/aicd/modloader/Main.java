package io.github.e9ae9933.aicd.modloader;

import java.io.File;

public class Main implements FileUtils
{
	void setupMod(File origin,File modDir) throws Exception
	{
		modDir.mkdirs();
		File cacheDir=new File(modDir,"cache");
		cacheDir.mkdirs();
		xcopy(origin,new File(cacheDir,"origin"));
		xcopy(new File(cacheDir,"origin"),new File(cacheDir,"running"));
		File runningDir=new File(cacheDir,"running");
		runningDir.mkdirs();
		File runningPxls=new File(runningDir,"pxlsExported");
		runningPxls.mkdirs();
		io.github.e9ae9933.aicd.pxlskiller.Main.main(
				new String[]{
						"--delete",
						"--output",runningPxls.getAbsolutePath(),
						"--dir",new File(runningDir,"TextAsset").getAbsolutePath(),
						"--textureDir",new File(runningDir,"Texture2D").getAbsolutePath()});
		xcopy(runningDir,new File(cacheDir,"master"));

	}
}
