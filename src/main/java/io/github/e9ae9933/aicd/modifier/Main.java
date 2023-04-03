package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.Policy;
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
		/*
		NoelByteBuffer buf=new NoelByteBuffer();
		buf.putDouble(0x0123456789ABCDEFL*1.0);
		System.out.println(Double.toString(buf.getDouble()));
		if(true)
			return;*/
		InputStream is=new FileInputStream("src/main/resources/version15.yml");
		byte[] b=new byte[is.available()];
		is.read(b);
		is.close();
		String s=new String(b, StandardCharsets.UTF_8);
		Load load=new Load(LoadSettings.builder().build());
		Object o=load.loadFromString(s);
		System.out.println(o+" "+o.getClass());
		Map<String,Object> rawtype= (Map<String, Object>) ((Map)o).get("types");
		Map<String,Object> segments= (Map<String, Object>) ((Map)o).get("list");

		Map<String,Type> types=new LinkedHashMap<>();
		for(Map.Entry<String,Object> entry:rawtype.entrySet())
		{
			types.put(entry.getKey(),new Type(entry.getKey(),types, (Map<String, Object>) entry.getValue()));
		}

		System.out.println(Policy.gson.toJson(types));

		FileInputStream test=new FileInputStream("src/main/resources/savedata_00.aicsave");
		NoelByteBuffer testBytes=new NoelByteBuffer(test);
		test.close();
		Map<String,NoelElement> elements=new LinkedHashMap<>();
		for(Map.Entry<String,Object> entry:segments.entrySet())
		{
			String name=entry.getKey();
			Object value=entry.getValue();
			System.out.println("handling "+name);
			if(value instanceof String)
			{
				Type type=types.get(value);
				elements.put(name,type.read(testBytes,null,types));
			}
			else
			{
				Map<String,Object> settings= (Map<String, Object>) value;
				settings.put("known_elements",elements);
				Type type=types.get(settings.get("type").toString());
				elements.put(name,type.read(testBytes,settings,types));
			}
		}
		System.out.println(Policy.gson.toJson(elements));
		elements.forEach((str,e)->{if(str.contains("expected"))System.out.println(str+" "+Policy.gson.toJson(e));});
		System.out.println("Left size "+testBytes.size());
	}
}
