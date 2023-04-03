package io.github.e9ae9933.aicd.modifier;

public class NoelUnsignedInt extends NoelInt implements NoelLongable
{
	public NoelUnsignedInt(NoelByteBuffer b)
	{
		super(b);
	}

	@Override
	public long getLong()
	{
		return Integer.toUnsignedLong(data);
	}

	@Override
	public void setLong(long l)
	{
		if(l<0||l>=1L<<Integer.SIZE)
			throw new IllegalArgumentException(l+" is not an unsigned integer");
		data= (int) l;
	}
}
