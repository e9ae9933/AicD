package io.github.e9ae9933.aicd.modifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoelArray extends NoelElement
{
	long len;
	int lenSize;
	List<NoelElement> data;
	public NoelArray(NoelByteBuffer b, Map<String,Object> settings,Map<String,Class<? extends NoelElement>> primitives,Map<String,NoelElement> variables)
	{
		if(settings==null)
			throw new IllegalArgumentException("Array must have settings");
		len=Long.parseLong(settings.getOrDefault("len",-1).toString());
		long length=len;
		if(len==-1)
		{
			Object lensz=settings.get("lensize");
			if(lensz==null)
				throw new RuntimeException("null lensize");
			lenSize=Integer.parseInt(lensz.toString());
			length=0;
			for(int i=0;i<lenSize;i++)
				length=length*256+(b.getByte()&0xFF);
		}

		Object o=settings.get("value");
		data =new ArrayList<>();
		for(int i=0;i<length;i++)
			data.add(NoelElement.newInstance(o,b,primitives,variables));
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		if(len==-1)
		{
			//write length
			int length= data.size();
			for(int i=lenSize-1;i>=0;i--)
				b.putByte((byte) ((length>>(i*8))&0xFF));
		}
		for(NoelElement e: data)
			e.writeTo(b);
	}

	@Override
	public Component createGUI()
	{
		return unsupportedGUI();
	}
}
