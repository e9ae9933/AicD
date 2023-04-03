package io.github.e9ae9933.aicd.modifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NoelObject extends NoelElement
{
	Map<String,NoelElement> data;
	public NoelObject(Map<String,NoelElement> data)
	{
		this.data=data;
	}
	public NoelObject(NoelByteBuffer b,Map<String,Object> settings,Map<String,Type> knownTypes)
	{
		if(settings==null)
			throw new IllegalArgumentException("osn");
		data=new LinkedHashMap<>();
		Map<String,Object> values= (Map<String, Object>) settings.get("types");
		for(Map.Entry<String,Object> entry:values.entrySet())
		{
			String name=entry.getKey();
			Object o=entry.getValue();
			Type type=knownTypes.get(Type.readTypeNameFromSettings(o));
			data.put(name,type.read(b,Type.toSettings(o),knownTypes));
		}
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		for(NoelElement e:data.values())
			e.writeTo(b);
	}
}
