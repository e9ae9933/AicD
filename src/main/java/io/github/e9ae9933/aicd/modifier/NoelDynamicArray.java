package io.github.e9ae9933.aicd.modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoelDynamicArray extends NoelElement
{
	NoelElement len;
	Type type;
	List<NoelElement> data;
	public NoelDynamicArray(NoelByteBuffer b, Map<String,Object> settings,Map<String,Type> knownTypes)
	{
		Map<String,NoelElement> elements= (Map<String, NoelElement>) settings.get("known_elements");
		len=elements.get(settings.get("len").toString());
		if(!(len instanceof NoelLongable))
			throw new IllegalArgumentException("len "+len+" is not a NoelLongable");
		long length=((NoelLongable) len).getLong();
		type=knownTypes.get(settings.get("value").toString());
		if(type==null)
			throw new IllegalArgumentException("null type on "+settings.get("value"));
		data=new ArrayList<>();
		for(int i=0;i<length;i++)
			data.add(type.read(b,settings,knownTypes));
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		long length= ((NoelLongable) len).getLong();
		for(int i=0;i<length;i++)
			data.get(i).writeTo(b);
	}
}
