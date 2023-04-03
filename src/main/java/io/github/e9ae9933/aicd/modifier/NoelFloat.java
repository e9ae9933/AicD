package io.github.e9ae9933.aicd.modifier;

public class NoelFloat extends NoelElement
{
	float data;
	public NoelFloat(NoelByteBuffer b)
	{
		data=b.getFloat();
	}

	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putFloat(data);
	}
}
