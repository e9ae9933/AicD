package io.github.e9ae9933.aicd.modloader;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;

public class AICLauncher
{
	public static void main(String[] args)
	{
		OptionParser optionParser=new OptionParser();
		ArgumentAcceptingOptionSpec<String> dirOption=optionParser.accepts("dir").withRequiredArg().required();
		ArgumentAcceptingOptionSpec<String> gitOption=optionParser.accepts("git").withRequiredArg();
		OptionSpec<Void> runOption=optionParser.accepts("run");
		OptionSpec<Void> forceOption=optionParser.accepts("force");
		OptionSpec<Void> blockOption=optionParser.accepts("block");
		OptionSet optionSet=optionParser.parse(args);
		boolean run=optionSet.has(runOption);
		File dir=new File(optionSet.valueOf(dirOption));
		if(optionSet.has(gitOption))
		{
			Git.targetGit=new File(optionSet.valueOf(gitOption));
			if(!Git.targetGit.isFile())
				throw new IllegalArgumentException("the git "+Git.targetGit+" is not a file");
		}
		if(!dir.isDirectory())
			throw new IllegalArgumentException(dir+" is not a directory");
		RedirectHandler handler=new RedirectHandler(dir);
		boolean needUpdate=false;
		if(!handler.checkNeedUpdate())
		{
			if(!optionSet.has(forceOption))
			{
				System.out.println("No need to update");
				return;
			}
			System.out.println("Force update");
			needUpdate=true;
		} else needUpdate=true;
		if(needUpdate)
		{
			System.out.println("Updating...");
			try
			{
				handler.updateMods();
			} catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Update failed");
				System.exit(1);
			}
		}
		System.out.println("Launching...");
		if(run)
			try
			{
				Process p=Runtime.getRuntime().exec(new File(dir,"AliceInCradle.exe").getAbsolutePath(), null, dir);
				if(optionSet.has(blockOption))
				{
					System.out.println("Blocking until exit...");
					int rt=p.waitFor();
					System.out.println("AliceInCradle.exe returned "+rt);
				}
			}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Launch failed");
		}
		System.out.println("It seems like it is successful.");
		System.out.println("Exiting program.");
		return;
	}
}
