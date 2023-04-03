package io.github.e9ae9933.aicd.modifier;

public class NoelByte extends NoelElement implements NoelLongable
{
	byte data;
	public NoelByte(NoelByteBuffer b)
	{
		data=b.getByte();
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putByte(data);
	}

	@Override
	public long getLong()
	{
		return data;
	}

	@Override
	public void setLong(long l)
	{
		if(l<Byte.MIN_VALUE||l>Byte.MAX_VALUE)
			throw new IllegalArgumentException(l+" is not a byte");
		data= (byte) l;
	}
}
