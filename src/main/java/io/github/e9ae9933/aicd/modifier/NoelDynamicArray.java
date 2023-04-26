package io.github.e9ae9933.aicd.modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoelDynamicArray extends NoelElement
{
	NoelElement len;
	List<NoelElement> data;
	public NoelDynamicArray(NoelByteBuffer b, Map<String,Object> settings,Map<String,Class<? extends NoelElement>> primitives,Map<String,NoelElement> variables)
	{
		len=variables.get(settings.get("len").toString());
		if(!(len instanceof NoelLongable))
			throw new IllegalArgumentException("len "+len+" is not a NoelLongable");
		long length=((NoelLongable) len).getLong();
		data=new ArrayList<>();
		Object o=settings.get("value");
		for(int i=0;i<length;i++)
			data.add(NoelElement.newInstance(o,b,primitives,variables));
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		long length= ((NoelLongable) len).getLong();
		for(int i=0;i<length;i++)
			data.get(i).writeTo(b);
	}
}
