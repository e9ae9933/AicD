package io.github.e9ae9933.aicd.modifier;

public class NoelShort extends NoelElement implements NoelLongable
{
	short data;
	public NoelShort(NoelByteBuffer b)
	{
		data=b.getShort();
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putShort(data);
	}

	@Override
	public long getLong()
	{
		return data;
	}

	@Override
	public void setLong(long l)
	{
		if(l<Short.MIN_VALUE||l>Short.MAX_VALUE)
			throw new IllegalArgumentException(l+" is not a short");
		data= (short) l;
	}
}
