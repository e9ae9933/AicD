package io.github.e9ae9933.aicd.modloader;

import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AssetsInfo implements FileUtils
{
	long unpackTime;
	String unpackTimeFormatted;
	String aliceInCradleVersion;
	String aicUtilsVersion;
	ConcurrentHashMap<String,String> gitHashes;
	AssetsInfo(Assets assets)
	{
		File unpackInfo=new File(assets.getDir(),"info.yml");
		if(!unpackInfo.isFile())
			throw new RuntimeException("no "+unpackInfo+" found");
//		File unpackInfo=new File(assets.getDir().getParentFile(),"cache/origin/info.yml");
		Map<String,Object> infos= (Map<String, Object>) Policy.getLoad().loadFromString(Utils.readAllUTFString(unpackInfo));
		unpackTime=Long.parseLong(infos.get("time").toString());
		unpackTimeFormatted=infos.get("time_formatted").toString();
		aliceInCradleVersion=infos.get("aliceincradle_version").toString();
		aicUtilsVersion=infos.get("version").toString();
		gitHashes=new ConcurrentHashMap<>();
		walk(assets.dir,"");
	}
	void walk(File file,String s)
	{
		if(file.isDirectory())
		{
			Arrays.stream(file.listFiles()).parallel()
					.forEach(f->{
						walk(f,s+"/"+file.getName());
					});
		}
		else {
			gitHashes.put(s+"/"+file.getName(),gitHash(file));
		}
	}
}
