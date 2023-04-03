package io.github.e9ae9933.aicd.modifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class NoelMap extends NoelElement
{
	Type key;
	Type value;
	LinkedHashMap<NoelElement,NoelElement> data;
	public NoelMap(NoelByteBuffer b, Map<String,Object> settings,Map<String,Type> knownTypes)
	{
		if(settings==null)
			throw new IllegalArgumentException("Map must have settings");
		key=knownTypes.get(settings.get("key").toString());
		value=knownTypes.get(settings.get("value").toString());
		int len=Short.toUnsignedInt(b.getShort());
		data=new LinkedHashMap<>();
		for(int i=0;i<len;i++)
			data.put(key.read(b,settings,knownTypes),value.read(b,settings,knownTypes));
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putShort(((short) data.size()));
		for(Map.Entry<NoelElement,NoelElement> entry:data.entrySet())
		{
			entry.getKey().writeTo(b);
			entry.getValue().writeTo(b);
		}
	}
}
