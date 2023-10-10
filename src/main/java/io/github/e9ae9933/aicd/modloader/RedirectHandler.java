package io.github.e9ae9933.aicd.modloader;

import com.google.gson.reflect.TypeToken;
import io.github.e9ae9933.aicd.Constants;
import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.l10nkiller.MultiLanguageFamilies;
import io.github.e9ae9933.aicd.l10nkiller.RefreshedEventLoader;
import io.github.e9ae9933.aicd.pxlskiller.PxlCharacter;
import io.github.e9ae9933.aicd.pxlskiller.Settings;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

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
	File getRedirectOriginalTranslationsFile()
	{
		return new File(getRedirectDir(),"original_event_translations.json");
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
	File getStreamingAssetsDir()
	{
		return new File(aicDir,"AliceInCradle_Data/StreamingAssets");
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
			System.out.println("xcopy...");
			xcopy(getUnpackDir(), getRedirectAssetsDir());
			Assets assets=getRedirectAssets();
			assets.createDirectories();
			io.github.e9ae9933.aicd.pxlskiller.Main.main(
					new String[]{
							"--output",assets.getPxlsUnpackedDir().getAbsolutePath(),
							"--dir",assets.getTextAssetDir().getAbsolutePath(),
							"--textureDir",assets.getTexture2DDir().getAbsolutePath(),
//							"--delete",
							"--noExtra"
					}
			);
			getRedirectPxlsPackedDir().mkdirs();
			io.github.e9ae9933.aicd.pxlskiller.Rev.handle(
					new String[]{
							"--output",getRedirectPxlsPackedDir().getAbsolutePath(),
							"--dir",assets.getPxlsUnpackedDir().getAbsolutePath()
					},new File(getRedirectAssetsDir(),"Texture2D")
			);
			//todo: WHY??? so much memory
			System.gc();
			//
			assets.gitInit();
			getOriginalTranslationsDir().mkdirs();
			io.github.e9ae9933.aicd.l10nkiller.Main.main(
					new String[]{
							"--dir",new File(aicDir,"AliceInCradle_Data/StreamingAssets/localization").getAbsolutePath(),
							"--list",new File(new Assets(getUnpackDir()).getTextAssetDir(),"__tx_list").getAbsolutePath(),
							"--output",getOriginalTranslationsDir().getAbsolutePath()
					}
			);
			System.out.println("loading event translations...");
			MultiLanguageFamilies mlf=RefreshedEventLoader.loadWholeFromAIC(aicDir);
			RefreshedEventLoader.writeMultiLanguageFamiliesToJson(getRedirectOriginalTranslationsFile(),mlf);
			System.out.println("xcopy...");
			xcopy(getOriginalTranslationsDir(),getRedirectTranslationsDir());
			RedirectInfo info=new RedirectInfo(RedirectHandler.this);
//			info.modsMD5=null;
			Utils.writeAllUTFString(getRedirectInfoFile(), Policy.getGson().toJson(info));
		});
	}
	boolean checkNeedUpdate()
	{
		RedirectInfo info= Policy.getGson().fromJson(Utils.readAllUTFString(getRedirectInfoFile()),RedirectInfo.class);
//		System.out.println(info.modsMD5);
		if(info.needUpdate)
			return true;
		if(info.modsMD5==null)
			return true;
		if(!info.modsMD5.equalsIgnoreCase(md5Dir(getModsDir())))
			return true;
		return false;
	}
	void refreshOrigin()
	{
		Utils.writeAllUTFString(new File(getWorkDir(),"refreshOrigin"),"refreshOrigin");
		Utils.ignoreExceptions(()->{
//			Process aic = Runtime.getRuntime().exec(new File(aicDir, "AliceInCradle.exe").getAbsolutePath(),null,);
			File dir=aicDir;
			Process p=Runtime.getRuntime().exec(new File(dir,"AliceInCradle.exe").getAbsolutePath(), null, dir);
			int rt=p.waitFor();
			System.out.println("aic returned "+rt);
		});
	}
	void updateMods()
	{
		Utils.ignoreExceptions(()->
		{
			getModsDir().mkdirs();
			Assets assets = getRedirectAssets();
			Git git=assets.getGit();
			System.out.println("git reset");
			git.call("reset",
					"--hard",
					"Original"
					);
			System.out.println("git clean");
			git.call("clean",
					"--force",
					"-d");
			System.out.println("loading translations");

			ConcurrentHashMap<String, Map<String,String>> trans=new ConcurrentHashMap<>();
			Arrays.stream(getOriginalTranslationsDir().listFiles())
//					.parallel()
					.filter(f->f.isFile())
					.forEach(
							f->{
								System.out.println("loading "+f);
								trans.put(f.getName(),Mod.readTranslationFile(f));
							}
					);

			MultiLanguageFamilies multiLanguageFamilies= RefreshedEventLoader.loadMultiLanguageFamiliesFromJson(getRedirectOriginalTranslationsFile());

			System.out.println("loading dll");
			Arrays.stream(getPluginsDir().listFiles())
					.parallel()
					.filter(f->f.isFile()&&f.getName().endsWith(".dll"))
					.forEach(f->f.delete());
			RedirectInfo info= Policy.getGson().fromJson(Utils.readAllUTFString(getRedirectInfoFile()),RedirectInfo.class);
			List<String> fails=new ArrayList<>();
			Arrays.stream(getModsDir().listFiles())
					.filter(f->f.isFile()&&f.getName().endsWith(".zip"))
					.forEachOrdered(f->{
						try{
							ZipFile file=new ZipFile(f);

							if(file.getEntry("info.json")==null)
							{
								System.out.println("not a mod: "+f);
								file.close();
								return;
							}
							Mod mod= Policy.getGson().fromJson(Utils.readAllUTFString(file.getInputStream(file.getEntry("info.json"))),Mod.class);
							// patch.patch
							if(file.getEntry("patch.patch")!=null)
							{
								byte[] patch = Utils.readAllBytes(file.getInputStream(file.getEntry("patch.patch")));
								ByteArrayInputStream bais=new ByteArrayInputStream(patch);
								File temp = new File(getRedirectDir(), "tmp");
								FileOutputStream fos=new FileOutputStream(temp);
								Scanner cin=new Scanner(bais);
								while(cin.hasNextLine())
								{
									String s=cin.nextLine();
									if(s.startsWith("diff --git ")&&Constants.shouldWeHandlePxls)
									{
										//wowie
										String r=s.substring(11);
										String dir;
										if(r.contains("\""))
										{
											int ll=r.indexOf("\"");
											int rr=r.indexOf("\"",ll+1);
											dir=r.substring(ll+1,rr);
										}
										else dir=r.split(" ")[0];
										String realDir=dir.substring(dir.indexOf("/")+1);
										//how to?
										CharArrayWriter baos=new CharArrayWriter();
										char[] c=realDir.toCharArray();
										for(int i=0;i<c.length;i++)
										{
											if(c[i]=='\\')
											{
												int l=Integer.parseInt(new String(new char[]{c[i+1],c[i+2],c[i+3]}),8);
												baos.write(l);
												i+=3;
											}
											else {
												baos.write(c[i]);
											}
										}
										String unescapedDir=baos.toString();
										System.out.println("unescaped "+s+" into "+unescapedDir);
										fos.write((s+"\n").getBytes(StandardCharsets.UTF_8));
										String index=cin.nextLine();
										if(!Pattern.compile("[0-9a-f]{40}").matcher(index).find())
										{
											//fall back
											fos.write((index+"\n").getBytes(StandardCharsets.UTF_8));
											continue;
										}
										if(!index.startsWith("index "))throw new RuntimeException("not index???");
										String[] t=index.split("[0-9a-f]{40}",2);
										String trickyHash=gitHash(new File(getRedirectAssetsDir(),unescapedDir));
										fos.write((t[0]+trickyHash.toLowerCase()+t[1]+"\n").getBytes(StandardCharsets.UTF_8));
									}
									else fos.write((s+"\n").getBytes(StandardCharsets.UTF_8));
								}
								fos.close();
								cin.close();
								bais.close();
								int rt=git.call(
										"apply",
//										"--index",
										"--binary",
										"-3",
										"--allow-empty",
										"-v",
										temp.getAbsolutePath()
								);
								if(rt!=0)
									fails.add(mod.name);
//								temp.delete();
							}

							// translations.json
							if(file.getEntry("translations.json")!=null)
							{
								Map<String, Map<String, String>> trmap = Policy.getGson().fromJson(Utils.readAllUTFString(file.getInputStream(file.getEntry("translations.json"))), new TypeToken<Map<String, Map<String, String>>>()
								{
								});
								trmap.forEach(
										(k, v) ->
										{
											if (trans.containsKey(k))
												trans.get(k).putAll(v);
											else trans.put(k, v);
										}
								);
							}
							if(file.getEntry("events.json")!=null)
							{
								String str=Utils.readAllUTFString(file.getInputStream(file.getEntry("events.json")));
								MultiLanguageFamilies diff=Policy.getGson().fromJson(str,MultiLanguageFamilies.class);
								multiLanguageFamilies.merge(diff);
							}

							//plugin.dll
							if(file.getEntry("plugin.dll")!=null)
							{
								InputStream is=file.getInputStream(file.getEntry("plugin.dll"));
								byte[] b=Utils.readAllBytes(is);
								Utils.writeAllBytes(new File(getPluginsDir(),mod.name+".dll"),b);
								is.close();
							}

							file.close();
						}catch (Exception e){
							new RuntimeException("failed to load "+f.getName(),e).printStackTrace();
							fails.add(f.getName());
						};
					});
			if(fails.size()>0)
			{
				int chs=JOptionPane.showConfirmDialog(null, String.format(L10n.GIT_FAILED.toString(), fails.toString()),L10n.GIT_FAILED_TITLE.toString(), JOptionPane.OK_CANCEL_OPTION,JOptionPane.WARNING_MESSAGE);
				if(chs!=0)
				{
//					Thread.currentThread().stop();
					throw new RuntimeException("User cancelled launching");
//					Thread.currentThread().interrupt();
				}
			}
			System.out.println("checking translations...");
			trans.forEach(
					(k,v)->{
						System.out.println("checking "+k);
						Utils.writeAllUTFString(new File(getRedirectTranslationsDir(),k), Policy.getDump().dumpToString(v));
					}
			);
			System.out.println("event translations writing");
			File evfile=new File(getRedirectDir(),"event_translations.yml");
			Utils.writeAllUTFString(evfile, Policy.getDump().dumpToString(multiLanguageFamilies));
			if(!Constants.shouldWeHandlePxls)
			{

			}
			else
			{
				System.out.println("rebuilding pxls...");
				List<File> toBeRebuilt = Arrays.stream(Objects.requireNonNull(getRedirectAssets().getPxlsUnpackedDir().listFiles(), "null " + getRedirectAssets().getPxlsUnpackedDir()))
						.parallel()
						.filter(f -> f.isDirectory())
						.filter(f ->
						{
							String original = info.pxlsUnpackedCache.originalMD5.get(f.getName());
							if (original == null)
								return true;//new one
							return !original.equals(md5Dir(f));
						}).collect(Collectors.toList());
				List<File> changedPxls = info.pxlsCache.getChangedFiles(getRedirectPxlsPackedDir());
				System.out.println("changed pxls: " + changedPxls);
				changedPxls.stream().parallel()
						.forEach(f ->
						{
							File target = new File(getRedirectAssets().getPxlsUnpackedDir(), f.getName().substring(0, f.getName().length() - 5));
							f.delete();
							if (target.exists())
								toBeRebuilt.add(target);
						});
				System.out.println("to be rebuilt: " + toBeRebuilt.stream().distinct().collect(Collectors.toList()));
				toBeRebuilt.stream()
						.distinct()
						.parallel()
						.forEach(f ->
						{
							System.out.println("output " + f);
							Settings s = new Settings();
							PxlCharacter chara = new PxlCharacter(f, s);
							Utils.writeAllBytes(new File(getRedirectPxlsPackedDir(), f.getName() + ".pxls"), chara.outputAsBytes(new Settings()));
							File t2d = new File(getRedirectAssetsDir(), "Texture2D");
							File target = new File(t2d, f.getName() + ".pxls.bytes.texture_0.png");
							Utils.writeAllBytes(target, s.exportPng);
							System.out.println("okay output " + f);
						});
			}
			File specialPatch=new File(getRedirectAssets().getTextAssetDir(),"__tx_list");
			Utils.writeAllUTFString(specialPatch,"_aicutils_translation");
			System.out.println("reading info");
			info.modsMD5=md5Dir(getModsDir());
			info.needUpdate=false;
			Utils.writeAllUTFString(getRedirectInfoFile(), Policy.getGson().toJson(info));
			System.out.println("All done");
		});
	}
}
