package io.github.e9ae9933.aicd.modifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class NoelMapEndSigned extends NoelElement
{
	Type key;
	Type value;
	LinkedHashMap<NoelElement,NoelElement> data;
	public NoelMapEndSigned(NoelByteBuffer b, Map<String,Object> settings, Map<String,Type> knownTypes)
	{
		if(settings==null)
			throw new IllegalArgumentException("Map must have settings");
		key=knownTypes.get(settings.get("key").toString());
		value=knownTypes.get(settings.get("value").toString());
//		int len=Short.toUnsignedInt(b.getShort());
		data=new LinkedHashMap<>();
		byte front1,front2;
		front1=b.getByte();
		front2=b.getByte();
		while(front1!=0||front2!=0)
		{
			b.putFront(front2);
			b.putFront(front1);
			data.put(key.read(b, settings, knownTypes), value.read(b, settings, knownTypes));
			front1=b.getByte();
			front2=b.getByte();
		}
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
//		b.putShort(((short) data.size()));
		for(Map.Entry<NoelElement,NoelElement> entry:data.entrySet())
		{
			entry.getKey().writeTo(b);
			entry.getValue().writeTo(b);
		}
		b.putByte((byte) 0);
		b.putByte((byte) 0);
	}
}
