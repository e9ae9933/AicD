package io.github.e9ae9933.aicd.l10nkiller;

import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
class test2
{
//	public static void main(String[] args)
//	{
//		System.out.println(EventLoader.diff(
//			EventLoader.loadRipeFromDir(new File("test")),
//				EventLoader.loadRipeFromFile(new File("test2.yml"))
//		));
//	}
}
@Deprecated
public class EventLoader
{
//	public static Map<String,Map<String,Map<String, List<String>>>> loadFromParentDir(File dir)
//	{
//		ConcurrentHashMap<String,Map<String,Map<String, List<String>>>> rt=new ConcurrentHashMap<>();
//		Arrays.stream(dir.listFiles())
//				.filter(f->f.isDirectory())
//				.forEachOrdered(f->{
//					rt.put(f.getName(),loadRipeFromDir(f));
//				});
//		return rt;
//	}
//	public static Map<String,Map<String,List<String>>>
//	readRawEvents(File dir, boolean needCheckName)
//	{
//		if(!dir.exists())
//			throw new RuntimeException(dir+" not exists");
//		ConcurrentHashMap<String, Map<String, List<String>>> rt = new ConcurrentHashMap<>();
//		Arrays.stream(dir.listFiles())
//				.parallel()
//				.filter(f->f.isFile())
//				.filter(f->!needCheckName||f.getName().startsWith("ev_")&&f.getName().endsWith(".txt"))
//				.forEach(f->{
//					rt.putAll(loadRipeFromFile(f));
//				});
//		return rt;
//	}
//	static Map<String,Map<String,List<String>>> loadRipeFromDir(File dir)
//	{
//		Map<String,Map<String,List<String>>> rt=new LinkedHashMap<>();
//		Arrays.stream(dir.listFiles())
//				.filter(f->f.isFile())
//				.forEachOrdered(f->
//						apply(rt,loadRipeFromFile(f)));
//		return rt;
//	}
//	static Map<String,Map<String,List<String>>> loadRipeFromFile(File file)
//	{
//		return (Map<String, Map<String, List<String>>>) Policy.getLoad().loadFromString(Utils.readAllUTFString(file));
//	}
//	static void writeToSingleFileWithNameChecked(File dir,File target)
//	{
//		String s= Policy.getDump().dumpToString(readRawEvents(dir,true));
//		Utils.writeAllUTFString(target,s);
//	}
//	static void writeToMultiFileWithNameChecked(File dir,File target)
//	{
//		target.mkdirs();
//		Map<String,Map<String,List<String>>> buf= readRawEvents(dir,true);
//		buf.forEach((s,map)->{
//			Map<String,Map<String,List<String>>> tmp=new LinkedHashMap<>();
//			tmp.put(s,map);
//			Utils.writeAllUTFString(new File(target,"ev_"+s.replaceAll("/","_")+".txt"), Policy.getDump().dumpToString(tmp));
//		});
//	}
//	static void apply(Map<String,Map<String,List<String>>> target,Map<String,Map<String,List<String>>> patch)
//	{
//		patch.forEach((s1,m)->{
//			if(!target.containsKey(s1))
//				target.put(s1,new LinkedHashMap<>());
//			m.forEach((s2,l)->{
//				target.get(s1).put(s2,l);
//			});
//		});
//	}
//	public static void applyAll(Map<String, Map<String, Map<String, List<String>>>> target,Map<String, Map<String, Map<String, List<String>>>> patch)
//	{
//		patch.forEach((key,map)->{
//			if(!target.containsKey(key))
//				target.put(key,map);
//			else apply(target.get(key),map);
//		});
//	}
//	public static Map<String, Map<String, Map<String, List<String>>>> diffAll(Map<String, Map<String, Map<String, List<String>>>> origin,Map<String, Map<String, Map<String, List<String>>>> master)
//	{
//		Map<String, Map<String, Map<String, List<String>>>> rt=new LinkedHashMap<>();
//		master.forEach((key,map)->{
//			if(!origin.containsKey(key))
//				rt.put(key,map);
//			else rt.put(key,diff(origin.get(key),map));
//		});
//		return rt;
//	}
//	static Map<String,Map<String,List<String>>> diff(Map<String,Map<String,List<String>>> origin,Map<String,Map<String,List<String>>> master)
//	{
//		LinkedHashMap<String,Map<String,List<String>>> rt=new LinkedHashMap<>();
//		master.forEach((s1,m)->{
//			m.forEach((s2,l)->{
//				do
//				{
//					Map<String, List<String>> o1 = origin.get(s1);
//					if(o1==null) break;
//					List<String> l2=o1.get(s2);
//					if(l2==null) break;
//					if(!l.equals(l2))break;
//					return;
//				}
//				while(false);
//				if(!rt.containsKey(s1))
//					rt.put(s1,new LinkedHashMap<>());
//				Map<String,List<String>> m3=rt.get(s1);
//				m3.put(s2,l);
//			});
//		});
//		return rt;
//	}
//	static HashMap<String,LinkedHashMap<String,List<String>>> readFrom(File f)
//	{
//		return Utils.ignoreExceptions(()->{
//			FileInputStream fis=new FileInputStream(f);
//			Scanner cin=new Scanner(fis,"UTF-8");
//			HashMap<String,LinkedHashMap<String,List<String>>> rt=new HashMap<>();
//			HashMap<String,LinkedHashMap<String,List<StringJoiner>>> buf=new HashMap<>();
//			LinkedHashMap<String,List<StringJoiner>> targetMap=null;
//			List<StringJoiner> targetList=null;
//			StringJoiner target=null;
//			while (cin.hasNextLine())
//			{
//				String s=cin.nextLine().split("//")[0].trim();
//				if(s.isEmpty()||s.startsWith("//"))continue;
//				if(s.equals("*"))
//				{
//					targetList.add(target=new StringJoiner("\n"));
//				}
//				else if(s.startsWith("*")&&s.lastIndexOf("*")==0)
//				{
//					String[] split=s.substring(1).split(" ");
//					if(split.length==1)
//					{
//						targetMap.put(split[0], targetList = new ArrayList<>());
//						targetList.add(target=new StringJoiner("\n"));
//					}
//					else if(split.length==2)
//					{
//						String key=split[0];
//						if(!buf.containsKey(key))
//							buf.put(key,new LinkedHashMap<>());
//						targetMap=buf.get(key);
//						targetMap.put(split[1], targetList = new ArrayList<>());
//						targetList.add(target=new StringJoiner("\n"));
//					}
//					else throw new IllegalArgumentException("what? "+s);
//				}
//				else {
//					target.add(s);
//				}
//			}
//			buf.forEach((eventName,map)->{
//				LinkedHashMap<String,List<String>> toPut=new LinkedHashMap<>();
//				map.forEach((key,list)->{
//					toPut.put(key,list.stream().map(sj->sj.toString()).collect(Collectors.toList()));
//				});
//				rt.put(eventName,toPut);
//			});
//
//
//			cin.close();
//			fis.close();
//			return rt;
//		});
//	}
}
