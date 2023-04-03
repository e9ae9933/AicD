package io.github.e9ae9933.aicd.modifier;

import java.io.InputStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class NoelByteBuffer
{
	Deque<Byte> data;
	byte shift;
	public NoelByteBuffer()
	{
		data=new LinkedList<>();
		shift=0;
	}
	public NoelByteBuffer(byte[] b)
	{
		this();
		for(byte i:b)
			data.add(i);
	}
	public NoelByteBuffer(InputStream is)
	{
		this();
		try
		{
			int b;
			while ((b = is.read()) != -1)
				data.add((byte) b);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}
	public void addShift(byte add)
	{
		shift+=add;
	}
	public byte getByte()
	{
		return (byte) (data.poll()-shift);
	}
	public void putByte(byte b)
	{
		data.add((byte) (b+shift));
	}
	public void putFront(byte b)
	{
		data.addFirst((byte) (b+shift));
	}
	public int size()
	{
		return data.size();
	}
	public short getShort()
	{
		return (short) ((getByte()&0xFF)<<8|getByte()&0xFF);
	}
	public void putShort(short s)
	{
		putByte((byte) (s>>>8));
		putByte(((byte) s));
	}
	public int getInt()
	{
		return (getShort()&0xFFFF)<<16|getShort()&0xFFFF;
	}
	public void putInt(int i)
	{
		putShort((short) (i>>>16));
		putShort((short) i);
	}
	public long getLong()
	{
		return (getInt()&0xFFFFFFFFL)<<32|getInt()&0xFFFFFFFFL;
	}
	public void putLong(long l)
	{
		putInt((int) (l>>>32));
		putInt((int) l);
	}
	public float getFloat()
	{
		return Float.intBitsToFloat(getInt());
	}
	public void putFloat(float f)
	{
		putInt(Float.floatToRawIntBits(f));
	}
	public double getDouble()
	{
		return Double.longBitsToDouble(getLong());
	}
	public void putDouble(double d)
	{
		putLong(Double.doubleToRawLongBits(d));
	}
	public void getBytes(byte[] b)
	{
		for(int i=0;i<b.length;i++)
			b[i]=getByte();
	}
	public byte[] getNBytes(int len)
	{
		byte[] b=new byte[len];
		getBytes(b);
		return b;
	}
	public void putBytes(byte[] b)
	{
		for(int i=0;i<b.length;i++)
			putByte(b[i]);
	}
}
