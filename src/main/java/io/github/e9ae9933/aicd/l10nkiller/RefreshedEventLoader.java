package io.github.e9ae9933.aicd.l10nkiller;

import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class RefreshedEventLoader
{
	public static MultiLanguageFamilies loadMultiLanguageFamiliesFromDir(File dir)
	{
		MultiLanguageFamilies rt = new MultiLanguageFamilies();
		Arrays.stream(dir.listFiles())
				.parallel()
				.filter(f -> f.isDirectory())
				.forEach(f ->
				{
					Family fml = loadFamilyFromYmlDir(f);
					rt.put(f.getName(), fml);
				});
		return rt;
	}

	public static MultiLanguageFamilies loadWholeFromAIC(File aicDir)
	{
		// ___family_zh-cn.txt
		List<String> fNames = Arrays.stream(new File(aicDir,"AliceInCradle_Data/StreamingAssets/localization").listFiles())
				.parallel()
				.filter(f -> f.isFile())
				.map(f ->
				{
					String s = f.getName();
					if (!s.startsWith("___family_") || !s.endsWith(".txt")) return null;
					return s.substring(10, s.length() - 4);
				})
				.filter(s -> s != null)
				.collect(Collectors.toList());
		System.out.println("found trans "+fNames);
		MultiLanguageFamilies rt = new MultiLanguageFamilies();
		fNames.stream()
				.parallel()
				.forEach(name ->
				{
					File dir = new File(new File(aicDir,"AliceInCradle_Data/StreamingAssets/localization"), name);
					if (!dir.isDirectory()) return;
					Family family = new Family();
					Arrays.stream(dir.listFiles())
							.parallel()
							.filter(f -> f.isFile())
							.filter(f -> f.getName().startsWith("ev_") && f.getName().endsWith(".txt"))
							.forEach(f ->
							{
								System.out.println("merge "+f);
								family.merge(loadFamilyFromAIC(f));
							});
					rt.put(name, family);
				});
		return rt;
	}

	public static Family loadFamilyFromAIC(File file)
	{
		try
		{
			Scanner cin = new Scanner(file, "UTF-8");
			Family rt = new Family();
			SingleEvent targetEvent = null;
			Message message = null;
			while (cin.hasNextLine())
			{
				String ss = cin.nextLine().trim();
				String s;
				if(ss.contains("//"))
					s=ss.substring(0,ss.indexOf("//"));
				else s=ss;
				if(s.isEmpty())continue;
				if (s.equals("*")) message.add(null);
				else if (s.startsWith("*")/* && s.substring(1).split(" ").length <= 2*/&&s.lastIndexOf("*")==0)
				{
					String[] split = s.substring(1).split(" ");
//					System.out.println("split into "+Arrays.toString(split));
					if(split.length>2||split.length==0)
					{
						System.err.println("WARNING: found "+s);
					}
					message = new Message();
					message.add(null);
					if (split.length == 1)
						targetEvent.put(split[0], message);
					else if (split.length == 2)
					{
						if(!rt.containsKey(split[0]))
						{
							targetEvent = new SingleEvent();
							targetEvent.put(split[1], message);
							rt.put(split[0], targetEvent);
						}
						else {
							targetEvent = rt.get(split[0]);
							targetEvent.put(split[1], message);
//							rt.put(split[0], targetEvent);
						}
					}
				} else if (message.get(message.size() - 1) == null)
					message.set(message.size() - 1, s);
				else message.set(message.size() - 1, message.get(message.size() - 1) + '\n' + s);
			}
			return rt;
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void writeMultiLanguageFamiliesToDir(File dir, MultiLanguageFamilies mlf)
	{
		dir.mkdirs();
		mlf.forEach(1, (s, f) ->
		{
			File familyDir = new File(dir, s);
			familyDir.mkdirs();
			f.forEach(1, (k, e) ->
			{
				File target = new File(familyDir, "ev_" + k.replace("/", "_") + ".yml");
				Family single = new Family();
				single.put(k, e);
				String str = Policy.getDump().dumpToString(single);
				Utils.writeAllUTFString(target, str);
			});
		});
	}
	public static void writeFamilyToDir(File familyDir, Family f)
	{
		familyDir.mkdirs();
		f.forEach(1, (k, e) ->
		{
			File target = new File(familyDir, "ev_" + k.replace("/", "_") + ".yml");
			Family single = new Family();
			single.put(k, e);
			String str = Policy.getDump().dumpToString(single);
			Utils.writeAllUTFString(target, str);
		});
	}

	public static MultiLanguageFamilies loadMultiLanguageFamiliesFromJson(File file)
	{
		return Policy.getGson().fromJson(Utils.readAllUTFString(file), MultiLanguageFamilies.class);
	}

	public static void writeMultiLanguageFamiliesToJson(File file, MultiLanguageFamilies mlf)
	{
		Utils.writeAllUTFString(file, Policy.getGson().toJson(mlf));
	}

	public static Family loadFamilyFromYmlDir(File dir)
	{
		try
		{
			Family rt = new Family();
			Arrays.stream(dir.listFiles())
					.parallel()
					.filter(f -> f.isFile())
					.forEach(f -> rt.merge(loadFamilyFromYml(f)));
			return rt;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Something went wrong on "+dir,e);
		}
	}

	public static Family loadFamilyFromYml(File file)
	{
		Object obj = Policy.getLoad().loadFromString(Utils.readAllUTFString(file));
		return Policy.getGson().fromJson(Policy.getGson().toJson(obj), Family.class);
	}
}
