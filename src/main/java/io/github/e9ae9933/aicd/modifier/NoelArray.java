package io.github.e9ae9933.aicd.modifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoelArray extends NoelElement
{
	long len;
	int lenSize;
	Type type;
	List<NoelElement> list;
	public NoelArray(NoelByteBuffer b, Map<String,Object> settings,Map<String,Type> knownTypes)
	{
		if(settings==null)
			throw new IllegalArgumentException("Array must have settings");
		len=Long.parseLong(settings.getOrDefault("len",-1).toString());
		long length=len;
		if(len==-1)
		{
			lenSize=Integer.parseInt(settings.get("lensize").toString());
			length=0;
			for(int i=0;i<lenSize;i++)
				length=length*256+(b.getByte()&0xFF);
		}

		Object o=settings.get("arraytype");
		type=knownTypes.get(Type.readTypeNameFromSettings(o));
		list=new ArrayList<>();
		for(int i=0;i<length;i++)
			list.add(type.read(b,Type.toSettings(o),knownTypes));
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		if(len==-1)
		{
			//write length
			int length=list.size();
			for(int i=lenSize-1;i>=0;i--)
				b.putByte((byte) ((length>>(i*8))&0xFF));
		}
		for(NoelElement e:list)
			e.writeTo(b);
	}
}
