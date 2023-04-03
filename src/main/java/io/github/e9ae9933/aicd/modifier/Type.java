package io.github.e9ae9933.aicd.modifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Type
{
	String name;
	Class<? extends NoelElement> clazz;
	Map<String,Object> types;
	public Type(String name,Map<String,Type> known,Map<String,Object> object) throws Exception
	{
		this.name=name;
		if(object.containsKey("class"))
			clazz= (Class<? extends NoelElement>) Class.forName(object.get("class").toString());
		else if(object.containsKey("types"))
			types= (Map<String, Object>) object.get("types");
	}
	public boolean isNative()
	{
		if((clazz==null)==(types==null))
			throw new IllegalArgumentException("Why would clazz and types are both null or both nonnull?");
		return clazz!=null;
	}
	public NoelElement read(NoelByteBuffer b,Map<String,Object> settings,Map<String,Type> knownTypes)
	{
		if(isNative())
		{
			return NoelElement.newInstance(clazz,b,settings,knownTypes);
		}
		else
		{
			LinkedHashMap<String,NoelElement> map=new LinkedHashMap<>();
			for(Map.Entry<String,Object> entry:types.entrySet())
			{
				String name=entry.getKey();
				Object o=entry.getValue();
				Type type;
				if(o instanceof String)
					type=knownTypes.get(o);
				else
					type=knownTypes.get(((Map) o).get("type"));
				if(type==null)
					throw new IllegalArgumentException("unknown type "+name+" "+o);
				map.put(name,type.read(b,o instanceof Map? (Map<String, Object>) o :null,knownTypes));
			}
			return new NoelObject(map);
		}
	}
	public static String readTypeNameFromSettings(Object o)
	{
		if(o instanceof String)
			return o.toString();
		else
			return ((Map)o).get("type").toString();
	}
	public static Map<String,Object> toSettings(Object o)
	{
		if(o instanceof Map)
			return ((Map<String, Object>) o);
		else
			return null;
	}
}
