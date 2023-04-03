package io.github.e9ae9933.aicd.modifier;

public class NoelUnsignedByte extends NoelByte implements NoelLongable
{
	public NoelUnsignedByte(NoelByteBuffer b)
	{
		super(b);
	}

	@Override
	public long getLong()
	{
		return Byte.toUnsignedLong(data);
	}

	@Override
	public void setLong(long l)
	{
		if(l<0||l>=1L<<Byte.SIZE)
			throw new IllegalArgumentException(l+" is not an unsigned byte");
		data= (byte) l;
	}
}
