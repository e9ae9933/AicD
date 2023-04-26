package io.github.e9ae9933.aicd.modifier;

import java.nio.charset.StandardCharsets;

public class NoelPascalString extends NoelElement
{
	String data;
	public NoelPascalString(NoelByteBuffer b)
	{
		int len=b.getByte()&0xFF;
		byte[] bytes=new byte[len];
		b.getBytes(bytes);
		data =new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		byte[] bytes= data.getBytes(StandardCharsets.UTF_8);
		b.putByte((byte) bytes.length);
		b.putBytes(bytes);
	}
}
