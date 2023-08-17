package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

public class NoelZeroBuffer extends NoelByteBuffer
{
	NoelZeroBuffer()
	{

	}
	@Override
	public byte getByte()
	{
		return 0;
	}

	@Override
	public void putByte(byte b)
	{
		throw new UnsupportedOperationException("NoelZeroBuffer does not supports putByte");
	}

	@Override
	public void putFront(byte b)
	{
		if(b!=0)
			throw new UnsupportedOperationException("Only 0 is allowed to put into NoelZeroBuffer");
	}

	@Override
	public int size()
	{
		return 0;
	}
}
