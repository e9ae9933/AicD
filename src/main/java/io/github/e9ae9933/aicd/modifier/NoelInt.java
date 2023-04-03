package io.github.e9ae9933.aicd.modifier;

public class NoelInt extends NoelElement implements NoelLongable
{
	int data;
	public NoelInt(NoelByteBuffer b)
	{
		data=b.getInt();
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putInt(data);
	}

	@Override
	public long getLong()
	{
		return data;
	}

	@Override
	public void setLong(long l)
	{
		if(l<Integer.MIN_VALUE||l>Integer.MAX_VALUE)
			throw new IllegalArgumentException(l+" is not an int");
		data= (int) l;
	}
}
