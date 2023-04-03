package io.github.e9ae9933.aicd.modifier;

public class NoelLong extends NoelElement
{
	long data;
	public NoelLong(NoelByteBuffer b)
	{
		data=b.getLong();
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putLong(data);
	}
}
