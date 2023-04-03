package io.github.e9ae9933.aicd.modifier;

public class NoelUnsignedShort extends NoelShort implements NoelLongable
{
	public NoelUnsignedShort(NoelByteBuffer b)
	{
		super(b);
	}

	@Override
	public long getLong()
	{
		return Short.toUnsignedLong(data);
	}

	@Override
	public void setLong(long l)
	{
		if(l<0||l>=1L<<Short.SIZE)
			throw new IllegalArgumentException(l+" is not an unsigned short");
		data= (short) l;
	}
}
