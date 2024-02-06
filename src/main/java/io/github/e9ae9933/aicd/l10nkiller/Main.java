package io.github.e9ae9933.aicd.l10nkiller;

import io.github.e9ae9933.aicd.Utils;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.lang3.Validate;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		OptionParser optionParser=new OptionParser();
		ArgumentAcceptingOptionSpec<String> dir=optionParser.accepts("dir").withRequiredArg();
		ArgumentAcceptingOptionSpec<String> list=optionParser.accepts("list").withRequiredArg();
		ArgumentAcceptingOptionSpec<String> output=optionParser.accepts("output").withRequiredArg();
		OptionSet optionSet=optionParser.parse(args);
		File dirDir= new File(optionSet.valueOf(dir));
		File listFile=new File(optionSet.valueOf(list));
		File outputDir=new File(optionSet.valueOf(output));
		Validate.isTrue(dirDir.isDirectory());
		Validate.isTrue(listFile.isFile());
		Validate.isTrue(outputDir.isDirectory());
		if(dirDir.isDirectory()&&listFile.isFile()&&outputDir.isDirectory())
		{
			List<String> pending=Arrays.stream(getString(listFile).split("\n")).map(s->s.trim()).collect(Collectors.toList());
			System.out.println("Pending "+pending);
			File def=new File(dirDir,"_");
			if(!def.isDirectory())
				throw new IllegalArgumentException(def.getPath()+" is not a directory");
			Arrays.stream(dirDir.listFiles((f) ->
			{
				String s=f.getName();
				System.out.println("Found "+f.getPath()+" "+s);
				return f.isFile() && s.startsWith("___family_") && s.endsWith(".txt");
			})).forEach(f->{
				try
				{
					String originalName = f.getName();
					String name = originalName.substring(10, originalName.length() - 10 - 4+10);
					System.out.println("Found family " + name);
					File familyDir = new File(dirDir, name);
					LinkedHashMap<String, String> target = new LinkedHashMap<>();
					destruct(pending, target, familyDir, def, name);
					Dump dump = new Dump(DumpSettings.builder().build());
					String ans = dump.dumpToString(target);
					FileOutputStream fos = new FileOutputStream(new File(outputDir,name + ".yml"));
					fos.write(ans.getBytes(StandardCharsets.UTF_8));
					fos.close();
					//okay now we destruct ev
//					RefreshedEventLoader.loadWholeFromAIC()
				}
				catch (Exception e)
				{
					System.out.println("Failed on "+f.getPath());
//					e.printStackTrace();
					throw new RuntimeException(e);
				}
			});
		}
		else throw new IllegalArgumentException("something wrong with args");
	}
	static void destruct(List<String> pending,LinkedHashMap<String,String> trans,File dir,File def,String name)
	{
		pending.stream()
				.map(s->s.startsWith("!")?new File(def,name+s.substring(1)+".txt"):new File(dir,name+s+".txt"))
				.forEach(f->{
					//System.out.println("reading "+f.getPath());
					String str=getString(f);
					if(str!=null)
						destruct(trans,str);
				});
	}
	static void destruct(LinkedHashMap<String,String> trans,String str)
	{
Scanner cin=new Scanner(str);
		StringBuilder target=null;
		LinkedHashMap<String,StringBuilder> cache=new LinkedHashMap<>();
		String s=null;
		try
		{
			while (cin.hasNext())
			{
				s = cin.nextLine();
				//if (target == null && s.isEmpty()) continue;
				if (s.startsWith("%FAMILY")) continue;
				if (s.startsWith("//")) continue;
				if (s.startsWith("/*"))
					cache.put(split(s)[2], target = new StringBuilder());
				else if (s.startsWith("/*___"))
					cache.put(split(s)[1], target = new StringBuilder());
				else if (s.startsWith("%ITEM") || s.startsWith("%ITEMREEL") || s.startsWith("%RECIPE_REPLACE"))
				{
					cache.put(split(s,3)[0] + " " + split(s,3)[1], new StringBuilder(split(s,3).length >= 3 ? split(s,3)[2] : ""));
					target=null;
				}
				else if (s.startsWith("&&"))
				{
					String[] sss=split(s,2);
					if(sss.length==1)
					{
						//??? thank you hashino mizuha
						target=null;
						continue;
					}
					//24a fix: why is there a tab???
					cache.put(sss[0], new StringBuilder(trim2(sss[1])));
					target=null;
				}
				else
				{
					if(target!=null)
						target.append("\n" + trim2(s));
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("failed on something. "+s);
			for(int i=0;i<5;i++)
				if(cin.hasNextLine())
					System.out.println(cin.nextLine());
//			e.printStackTrace();
			throw new RuntimeException(e);
		}
		cache.forEach((ss,sb)->{
			String value=sb.toString().replace("\\n","\n");
			trans.put(ss,trim2(value));
		});
	}
	static String reg="[ \\f\\n\\r\\t\\v\u3000]";
	static String[] split(String s)
	{
		return s.split(reg);
	}
	static String[] split(String s,int l)
	{
		return s.split(reg,l);
	}

	static String trim2(String s)
	{
		int l=0,r=s.length();
		while(l<r)
			if(Character.isWhitespace(s.charAt(l)))
				l++;
			else
				break;
		while(l<r)
			if(Character.isWhitespace(s.charAt(r-1)))
				r--;
			else
				break;
		return s.substring(l,r);
	}
	static String getString(File file)
	{try
	{
		FileInputStream fis = new FileInputStream(file);
		byte[] b = Utils.readAllBytes(fis);
		fis.close();
		return new String(b, StandardCharsets.UTF_8);
	}
	catch (Exception e)
	{
		System.out.println("Unable to find "+file.getPath());
		return null;
	}
	}
}
