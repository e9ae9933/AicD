package io.github.e9ae9933.aicd.modifier;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NoelString extends NoelElement
{
	int len;
	String data;
	public NoelString(NoelByteBuffer b, Map<String,Object> settings,Map<String,Class<? extends NoelElement>> primitives,Map<String,NoelElement> variables)
	{
		if(settings!=null)
			len=Integer.parseInt(settings.getOrDefault("len",-1).toString());
		else
			len=-1;
		int length=len;
		if(length==-1)
			length=Short.toUnsignedInt(b.getShort());
		byte[] bytes=new byte[length];
		b.getBytes(bytes);
		data =new String(bytes, StandardCharsets.UTF_8);
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		byte[] bytes= data.getBytes(StandardCharsets.UTF_8);
		if(len==-1)
			b.putShort((short) bytes.length);
		b.putBytes(bytes);
	}
	@Override
	public String toString()
	{
		return data;
	}
}
