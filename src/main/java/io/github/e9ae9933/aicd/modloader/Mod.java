package io.github.e9ae9933.aicd.modloader;

import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

class Test
{
	public static void main(String[] args) throws Exception
	{
		File dir= new File("F:\\work\\AnotherPlugin");
//		Mod mod=Mod.readMod(dir);
		Mod mod=Mod.createMod("AnotherPlugin",dir);
		mod.initMod(new File("F:\\work\\unpack"), new File("F:\\AliceInCradle_Data\\StreamingAssets\\localization"));
		mod.getAssets().gitInit();
//		FileOutputStream fos=new FileOutputStream("test.zip");
//		mod.diffAll(fos);
//		fos.close();
	}
}
public class Mod implements FileUtils
{
	transient File dir;
	String name;
	String version;
	int versionId;
	String[] author;
	String info;
	int apiVersion;
	String[] depend;
	String[] softDepend;
	String assetsVersion;
	private Mod(){}
	public static Mod readMod(File dir)
	{
		if(dir.isDirectory())
		{
			return Utils.ignoreExceptions(()->
			{
				File file = new File(dir, "info.json");
				FileInputStream fis=new FileInputStream(file);
				Mod mod = Policy.gson.fromJson(Utils.readAllUTFString(fis),Mod.class);
				fis.close();
				mod.dir=dir;
				return mod;
			});
		}
		else
			throw new IllegalArgumentException("need to be a dir");
	}
	public static Mod createMod(String name,File dir)
	{
		Mod mod=new Mod();
		mod.name=name;
		mod.version="0.0.1-SNAPSHOT";
		mod.versionId=1;
		mod.author=new String[0];
		mod.info="Example mod info\nWell, put your mod info here.\nPlease use UTF-8.";
		mod.apiVersion=13;
		mod.depend=new String[0];
		mod.softDepend=new String[0];
		mod.dir=dir;
		return mod;
	}
	public void initMod(File unpacked,File translations)
	{
		System.out.println("creating directories");
		createDirectories();
		System.out.println("initializing assets");
		initAssets(unpacked);
		System.out.println("initializing translations");
		initTranslations(translations);
		System.out.println("initializing plugins");
		initPlugins();
		System.out.println("assets git init");
		getAssets().gitInit();
		System.out.println("construction info");
		Utils.ignoreExceptions(()->
		{
			FileOutputStream fos = new FileOutputStream(new File(dir, "info.json"));
			fos.write(Policy.gson.toJson(this).getBytes(StandardCharsets.UTF_8));
			fos.close();
			FileOutputStream fos2=new FileOutputStream(new File(dir,"pack.png"));
			BufferedImage icon=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
			Graphics g=icon.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0,0,16,16);
			ImageIO.write(icon,"png",fos2);
			fos2.close();
		});
	}
	public void createDirectories()
	{
		dir.mkdirs();
		getPluginDir().mkdirs();
		getAssetsDir().mkdirs();
		getTranslationsDir().mkdirs();
		getCacheDir().mkdirs();
		hide(getCacheDir());
		getCacheTranslationsDir().mkdirs();
		getCacheOriginAssets().getDir().mkdirs();
		getCacheMasterAssets().getDir().mkdirs();
	}
	public void initAssets(File unpacked)
	{
		Utils.ignoreExceptions(()->
		{
			Map<String,String> mp= (Map<String, String>) Policy.load.loadFromString(Utils.readAllUTFString(new File(unpacked,"info.yml")));
			assetsVersion=mp.get("aliceincradle_version");
			boolean createAssets=true;
			Assets origin = getCacheOriginAssets();
			if (!createAssets)
			{
				System.err.println("Found assets folder. skipping...");
			} else
			{
				xcopy(unpacked, getCacheOriginAssets().getDir());
				origin.getPxlsUnpackedDir().mkdirs();
				io.github.e9ae9933.aicd.pxlskiller.Main.main(
						new String[]{
								"--delete",
//								"--noExtra",
								"--output", origin.getPxlsUnpackedDir().getAbsolutePath(),
								"--dir", origin.getTextAssetDir().getAbsolutePath(),
								"--textureDir", origin.getTexture2DDir().getAbsolutePath()});

			}
			System.out.println("creating directories");
			Assets assets=getAssets();
			assets.createDirectories();
//			assets.getPxlsPackedDir().mkdirs();
			System.out.println("copying files");
			xcopy(origin.getTextAssetDir(),assets.getTextAssetDir());
			xcopy(origin.getSpriteDir(),assets.getSpriteDir());
			xcopy(origin.getTexture2DDir(),assets.getTexture2DDir());
			xcopy(origin.getPxlsUnpackedDir(),assets.getPxlsUnpackedDir());
		});
	}
	public void diffAll(OutputStream os)
	{
		Utils.ignoreExceptions(()->
		{
			ZipOutputStream zos=new ZipOutputStream(os);
			zos.setComment(Policy.gson.toJson(Mod.this));
			zos.putNextEntry(new ZipEntry("patch.patch"));
			//get changed pxls

			getAssets().gitDiff(zos,new File(getCacheDir(),"patch.patch"));

			zos.putNextEntry(new ZipEntry("translations.json"));
			Map<String,Map<String,String>> trans=diffTranslations();
			zos.write(Policy.gson.toJson(trans).getBytes(StandardCharsets.UTF_8));

			zos.putNextEntry(new ZipEntry("info.json"));
			zos.write(Policy.gson.toJson(Mod.this).getBytes(StandardCharsets.UTF_8));

			File plugin=new File(getCacheDir(),"plugin.dll");
			if(plugin.isFile())
			{
				zos.putNextEntry(new ZipEntry("plugin.dll"));
				zos.write(Utils.readAllBytes(plugin));
			}

			if(new File(getDir(),"pack.png").isFile())
			{
				zos.putNextEntry(new ZipEntry("pack.png"));
				zos.write(Utils.readAllBytes(new File(getDir(),"pack.png")));
			}

			zos.finish();

		});
	}
	public void initTranslations(File translations)
	{
		Utils.ignoreExceptions(()->
		{
			Assets origin = getCacheOriginAssets();
			io.github.e9ae9933.aicd.l10nkiller.Main.main(
					new String[]{
							"--dir",translations.getAbsolutePath(),
							"--list",new File(origin.getTextAssetDir(),"__tx_list").getAbsolutePath(),
							"--output",getCacheTranslationsDir().getAbsolutePath()
					}
			);
			xcopy(getCacheTranslationsDir(),getTranslationsDir());
		});
	}
	public void initPlugins()
	{
		Utils.ignoreExceptions(()->
		{
			Utils.writeAllUTFString(new File(getCacheDir(),"update.bat"),"echo this is update.bat");
			InputStream is = Utils.readFromResources("SamplePlugin.zip", false);
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry e;
			while((e=zis.getNextEntry())!=null)
			{
				if(e.isDirectory())
					new File(getPluginDir(),e.getName()).mkdirs();
				else
				{
					String str=Utils.readAllUTFString(zis);
					String wt=str.replaceAll("SamplePlugin",name);
					File file=new File(getPluginDir(),e.getName().replaceAll("SamplePlugin",name));
					FileOutputStream fos=new FileOutputStream(file);
					fos.write(wt.getBytes(StandardCharsets.UTF_8));
					fos.close();
				}
			}
			zis.close();
		});
	}
	public Map<String,Map<String,String>> diffTranslations()
	{
		createDirectories();
		ConcurrentHashMap<String,Map<String,String>> rt=new ConcurrentHashMap<>();
		ConcurrentHashMap<String,Map<String,String>> mem=new ConcurrentHashMap<>();
		Arrays.stream(getCacheTranslationsDir().listFiles())
				.parallel()
				.filter(f->f.isFile())
				.forEach(
						f->{
							mem.put(f.getName(),readTranslationFile(f));
						}
				);
		Arrays.stream(getTranslationsDir().listFiles())
//				.parallel()
				.filter(f->f.isFile())
				.forEachOrdered(f->{
					Map<String,String> now=readTranslationFile(f);
					Map<String,String> pre=mem.get(f.getName());
					if(pre==null)
						rt.put(f.getName(),now);
					else {
						Map<String,String> add=new LinkedHashMap<>();
						rt.put(f.getName(),add);
						now.forEach((k,v)->{
							if(!v.equals(pre.get(k)))
								add.put(k,v);
						});
					}
				});
		return rt;
	}
	public static synchronized Map<String,String> readTranslationFile(File file)
	{
		return Utils.ignoreExceptions(()->
		{
			FileInputStream fis=new FileInputStream(file);
			Map<String,String> rt= (Map<String, String>) Policy.load.loadFromString(Utils.readAllUTFString(fis));
			fis.close();
			return rt;
		});
	}
	public File getDir()
	{
		return dir;
	}
	public File getPluginDir()
	{
		return new File(dir,"plugin");
	}
	public File getAssetsDir()
	{
		return new File(dir,"assets");
	}
	public Assets getAssets()
	{
		return new Assets(getAssetsDir());
	}
	public File getTranslationsDir()
	{
		return new File(dir,"translations");
	}
	public File getPxlsCacheFile()
	{
		return new File(getCacheDir(),"pxlsCache.json");
	}
	public File getCacheDir()
	{
		return new File(dir,"cache");
	}
	public File getCacheTranslationsDir()
	{
		return new File(getCacheDir(),"originalTranslations");
	}
	public Assets getCacheOriginAssets()
	{
		return new Assets(new File(getCacheDir(),"origin"));
	}
	public Assets getCacheMasterAssets()
	{
		return new Assets(new File(getCacheDir(),"master"));
	}
}
