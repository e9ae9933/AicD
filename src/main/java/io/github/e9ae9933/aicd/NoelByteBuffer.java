package io.github.e9ae9933.aicd;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class NoelByteBuffer
{
	public static List<NoelByteBuffer> handlers;
	static {
		handlers=new ArrayList<>();
	}
	Deque<Byte> data;
	byte shift;
	public NoelByteBuffer()
	{
		data=new LinkedList<>();
		shift=0;
		if(handlers!=null)
			handlers.add(this);
	}
	public static void endAll()
	{
		handlers.forEach(buf->buf.end());
		handlers.clear();
	}
	public void end()
	{
		if(size()>0)
		{
			System.err.println("Warning: left "+size()+" byte(s)");
			List<Byte> l=data.stream().limit(32).collect(Collectors.toList());
			System.err.println("printing first "+l.size()+" bytes");
			for(Byte b:l)
				System.err.printf("%02X ",b);
			System.err.println();
			for(Byte b:l)
				System.err.printf("%s ",Character.isISOControl(b)?"..":" "+(char)b.byteValue());
			System.err.println();
			data=null;
		}
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
		if(data.isEmpty()&&false)
		{
			data.add((byte) 0);
			System.err.println("补0");
		}
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
	public byte[] getAllBytes()
	{
		return getNBytes(size());
	}
	public void putBytes(byte[] b)
	{
		for(int i=0;i<b.length;i++)
			putByte(b[i]);
	}
	public boolean getBoolean()
	{
		return getByte()!=0;
	}
	public String getUTFString()
	{
		return new String(getNBytes(Short.toUnsignedInt(getShort())), StandardCharsets.UTF_8);
	}
	public short getUnsignedByte()
	{
		return (short)(getByte()&0xFF);
	}
	public int getUnsignedShort()
	{
		return Short.toUnsignedInt(getShort());
	}
	public long getUnsignedInt()
	{
		return getInt()&0xFFFFFFFFL;
	}
	public NoelByteBuffer getSegment()
	{
		int len=getInt();
		return new NoelByteBuffer(getNBytes(len));
	}
	public String getString(int len)
	{
		return new String(getNBytes(len));
	}
}
