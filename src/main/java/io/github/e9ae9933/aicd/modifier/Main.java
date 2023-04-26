package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main
{
	public static void main(String[] args) throws Exception
	{
	}
	public static NoelObject debugLoad()
	{
		String s=new String(readAllBytes("src/main/resources/version15_refresh.yml"), StandardCharsets.UTF_8);
		Load load=new Load(LoadSettings.builder().build());
		Map<String,Object> o= (Map<String, Object>) load.loadFromString(s);
		Map<String,String> typesRaw= (Map<String, String>) o.get("types");
		Map<String,Class<? extends NoelElement>> primitives=new LinkedHashMap<>();
		typesRaw.forEach((key,value)-> Utils.ignoreExceptions(()->primitives.put(key, (Class<? extends NoelElement>) Class.forName(value))));


		NoelByteBuffer b = new NoelByteBuffer(readAllBytes("src/main/resources/savedata_02.aicsave"));
		NoelObject list= (NoelObject) NoelElement.newInstance(o.get("list"), b,primitives,new LinkedHashMap<>());
		//System.out.println(Policy.gson.toJson(list));
		System.out.println("left "+b.size()+" byte(s)");
		return list;
	}
	static byte[] readAllBytes(String filename)
	{
		try
		{
			InputStream is = new FileInputStream(filename);
			byte[] b = new byte[is.available()];
			is.read(b);
			is.close();
			return b;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
