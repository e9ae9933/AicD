package io.github.e9ae9933.aicd.modifier;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NoelPascalString extends NoelElement
{
	String s;
	public NoelPascalString(NoelByteBuffer b, Map<String,Object> settings,Map<String,Type> knownTypes)
	{
		int len=b.getByte()&0xFF;
		byte[] bytes=new byte[len];
		b.getBytes(bytes);
		s=new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		byte[] bytes=s.getBytes(StandardCharsets.UTF_8);
		b.putByte((byte) bytes.length);
		b.putBytes(bytes);
	}
}
