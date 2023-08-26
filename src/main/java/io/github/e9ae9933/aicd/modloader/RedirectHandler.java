package io.github.e9ae9933.aicd.modloader;

import com.google.gson.reflect.TypeToken;
import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.pxlskiller.PxlCharacter;
import io.github.e9ae9933.aicd.pxlskiller.Settings;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

class Test2
{
	public static void main(String[] args)
	{
		File aicDir=new File("F:\\");
		RedirectHandler handler=new RedirectHandler(aicDir);
		handler.createDirectories();
//		handler.initRedirect();
		System.out.println(handler.checkNeedUpdate());
		handler.updateMods();
	}
}
public class RedirectHandler implements FileUtils
{
	File aicDir;
	RedirectHandler(File aicDir)
	{
		this.aicDir=aicDir;
	}
	File getWorkDir()
	{
		return new File(aicDir,"work");
	}
	File getUnpackDir()
	{
		return new File(getWorkDir(),"unpack");
	}
	File getRedirectDir()
	{
		return new File(getWorkDir(),"redirect");
	}
	File getRedirectInfoFile()
	{
		return new File(getRedirectDir(),"info.json");
	}
	File getRedirectAssetsDir()
	{
		return new File(getRedirectDir(),"assets");
	}
	File getRedirectTranslationsDir()
	{
		return new File(getRedirectDir(),"translations");
	}
	File getRedirectPxlsPackedDir()
	{
		return new File(getRedirectDir(),"pxls");
	}
	File getModsDir()
	{
		return new File(aicDir,"mods");
	}
	File getPluginsDir()
	{
		return new File(getRedirectDir(),"plugins");
	}
	Assets getRedirectAssets()
	{
		return new Assets(getRedirectAssetsDir());
	}
	File getOriginalTranslationsDir()
	{
		return new File(getRedirectDir(),"originalTranslations");
	}
	void createDirectories()
	{
		getModsDir().mkdirs();
		getRedirectDir().mkdirs();
		getOriginalTranslationsDir().mkdirs();
		getPluginsDir().mkdirs();
		getRedirectAssetsDir().mkdirs();
		getRedirectPxlsPackedDir().mkdirs();
	}
	void initRedirect()
	{
		Utils.ignoreExceptions(()->
		{
			createDirectories();
			hide(getRedirectDir());
			xcopy(getUnpackDir(), getRedirectAssetsDir());
			Assets assets=getRedirectAssets();
			assets.createDirectories();
			io.github.e9ae9933.aicd.pxlskiller.Main.main(
					new String[]{
							"--output",assets.getPxlsUnpackedDir().getAbsolutePath(),
							"--dir",assets.getTextAssetDir().getAbsolutePath(),
							"--textureDir",assets.getTexture2DDir().getAbsolutePath(),
							"--delete",
							"--noExtra"
					}
			);
			RedirectInfo info=new RedirectInfo(RedirectHandler.this);
			info.modsMD5=null;
			Utils.writeAllUTFString(getRedirectInfoFile(), Policy.gson.toJson(info));
			assets.gitInit();
			getRedirectPxlsPackedDir().mkdirs();
			io.github.e9ae9933.aicd.pxlskiller.Rev.main(
					new String[]{
							"--output",getRedirectPxlsPackedDir().getAbsolutePath(),
							"--dir",assets.getPxlsUnpackedDir().getAbsolutePath()
					}
			);
			getOriginalTranslationsDir().mkdirs();
			io.github.e9ae9933.aicd.l10nkiller.Main.main(
					new String[]{
							"--dir",new File(aicDir,"AliceInCradle_Data/StreamingAssets/localization").getAbsolutePath(),
							"--list",new File(new Assets(getUnpackDir()).getTextAssetDir(),"__tx_list").getAbsolutePath(),
							"--output",getOriginalTranslationsDir().getAbsolutePath()
					}
			);
			xcopy(getOriginalTranslationsDir(),getRedirectTranslationsDir());
		});
	}
	boolean checkNeedUpdate()
	{
		RedirectInfo info=Policy.gson.fromJson(Utils.readAllUTFString(getRedirectInfoFile()),RedirectInfo.class);
//		System.out.println(info.modsMD5);
		if(info.modsMD5==null)
			return true;
		if(!info.modsMD5.equalsIgnoreCase(md5Dir(getModsDir())))
			return true;
		return false;
	}
	void updateMods()
	{
		Utils.ignoreExceptions(()->
		{
			getModsDir().mkdirs();
			Assets assets = getRedirectAssets();
			Git git=assets.getGit();
			git.call("reset",
					"--hard",
					"Original"
					);

			ConcurrentHashMap<String, Map<String,String>> trans=new ConcurrentHashMap<>();
			Arrays.stream(getOriginalTranslationsDir().listFiles())
					.parallel()
					.filter(f->f.isFile())
					.forEach(
							f->{
								trans.put(f.getName(),Mod.readTranslationFile(f));
							}
					);
			RedirectInfo info=Policy.gson.fromJson(Utils.readAllUTFString(getRedirectInfoFile()),RedirectInfo.class);
			Arrays.stream(getModsDir().listFiles())
					.filter(f->f.isFile()&&f.getName().endsWith(".zip"))
					.forEachOrdered(f->{
						Utils.ignoreExceptions(()->{
							ZipFile file=new ZipFile(f);
							// patch.patch
							byte[] patch=Utils.readAllBytes(file.getInputStream(file.getEntry("patch.patch")));
							File temp=new File(getRedirectDir(),"tmp");
							Utils.writeAllBytes(temp,patch);
							git.call(
									"apply",
									"--index",
									temp.getAbsolutePath()
							);
							temp.delete();

							// translations.json
							Map<String,Map<String,String>> trmap=Policy.gson.fromJson(Utils.readAllUTFString(file.getInputStream(file.getEntry("translations.json"))),new TypeToken<Map<String,Map<String,String>>>(){});
							trmap.forEach(
									(k,v)->{
										if(trans.containsKey(k))
											trans.get(k).putAll(v);
										else trans.put(k,v);
									}
							);

							file.close();
						});
					});
			trans.forEach(
					(k,v)->{
						Utils.writeAllUTFString(new File(getRedirectTranslationsDir(),k),Policy.dump.dumpToString(v));
					}
			);
			List<File> toBeRebuilt=Arrays.stream(getRedirectAssets().getPxlsUnpackedDir().listFiles())
					.parallel()
					.filter(f->f.isDirectory())
					.filter(f->{
						String original=info.pxlsUnpackedCache.originalMD5.get(f.getName());
						if(original==null)
							return true;//new one
						return !original.equals(md5Dir(f));
					}).collect(Collectors.toList());
			System.out.println("to be rebuilt: "+toBeRebuilt);
			toBeRebuilt.stream()
					.parallel()
					.forEach(f->{
						System.out.println("output "+f);
						Settings s=new Settings();
						PxlCharacter chara=new PxlCharacter(f,s);
						Utils.writeAllBytes(new File(getRedirectPxlsPackedDir(),f.getName()+".pxls"),chara.outputAsBytes());
						System.out.println("okay output "+f);
					});
			File specialPatch=new File(getRedirectAssets().getTextAssetDir(),"__tx_list");
			Utils.writeAllUTFString(specialPatch,"_aicutils_translation");
			System.out.println("reading info");
			info.modsMD5=md5Dir(getModsDir());
			Utils.writeAllUTFString(getRedirectInfoFile(),Policy.gson.toJson(info));
			System.out.println("All done");
		});
	}
}
