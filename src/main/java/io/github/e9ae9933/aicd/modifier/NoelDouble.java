package io.github.e9ae9933.aicd.modifier;

public class NoelDouble extends NoelElement
{
	double data;
	public NoelDouble(NoelByteBuffer b)
	{
		data=b.getDouble();
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putDouble(data);
	}
}
