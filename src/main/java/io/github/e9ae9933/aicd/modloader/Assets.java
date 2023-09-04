package io.github.e9ae9933.aicd.modloader;

import io.github.e9ae9933.aicd.Utils;

import java.io.File;
import java.io.OutputStream;

public class Assets implements FileUtils
{
	File dir;
	public Assets(File dir)
	{
		this.dir=dir;
	}
	//@Deprecated
	public void createDirectories()
	{
		getDir().mkdirs();
		getTexture2DDir().mkdirs();
		getSpriteDir().mkdirs();
		getTextAssetDir().mkdirs();
//		getPxlsPackedDir().mkdirs();
//		hide(getPxlsPackedDir());
		getPxlsUnpackedDir().mkdirs();
//		hide(getPxlsUnpackedDir());
	}
	public Git getGit()
	{
		return Utils.ignoreExceptions(()->{
			Git git=new Git(getDir());
			if(!getGitDir().isDirectory())
				git.call("init");
			File ignore=new File(getDir(),".gitignore");
			Utils.writeAllUTFString(ignore,"*_NO_NEED_TO_MODIFY.png\n/.gitignore");
			return git;
		});
	}
	public void gitInit()
	{
		Utils.ignoreExceptions(()->
		{
			System.out.println("git add");
			Git git=getGit();
			git.call(
					"add",
					".",
					"-v");
			System.out.println("git commit");
			git.call(
					"-c","committer.name=AliceInCradle toolbox",
					"-c","committer.email=null@aictoolbox.top",
					"-c","author.name=Maybe unpacked AliceInCradle",
					"-c","author.email=null@aliceincradle.null",
					"commit",
					"-a",
					"-m","Initial commit maybe originated from AliceInCradle assets",
					"--allow-empty",
					"-v"
					);
			System.out.println("git tag");
			git.call(
					"tag",
					"--annotate",
					"-m","Original files",
					"Original"
			);
			System.out.println("git gc");
			git.call("gc");
		});
	}
	public void gitDiff(OutputStream os,File temp)
	{
		System.out.println("git diff");
		Utils.ignoreExceptions(()->{
			Git git=getGit();
			git.call("restore","--staged",".");
			git.call("add","--intent-to-add",".");
			git.call("diff","-p","--binary",//"-3",
					"--output",temp.getAbsolutePath());
			os.write(Utils.readAllBytes(temp));
		});
	}
	public File getDir()
	{
		return dir;
	}
	public File getGitDir()
	{
		return new File(dir,".git");
	}
	public File getTexture2DDir()
	{
		return new File(dir,"Texture2D");
	}
	public File getSpriteDir()
	{
		return new File(dir,"Sprite");
	}
	public File getTextAssetDir()
	{
		return new File(dir,"TextAsset");
	}
	public File getPxlsUnpackedDir()
	{
		return new File(dir,"pxlsUnpacked");
	}
//	public File getPxlsPackedDir()
//	{
//		return new File(dir,"pxls");
//	}
}
