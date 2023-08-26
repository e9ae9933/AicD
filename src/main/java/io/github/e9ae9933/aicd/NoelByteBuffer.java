package io.github.e9ae9933.aicd;

import java.nio.charset.StandardCharsets;
import java.util.*;

class Test
{
	public static void main(String[] args)
	{
		Deque<Byte> real=new LinkedList<>();
		NoelByteBuffer buf=new NoelByteBuffer();
		Random random=new Random();
		for(int i=0;i<100000;i++)
		{
			byte nextByte=(byte)random.nextInt(256);
			if(random.nextInt(10)<5||real.isEmpty())
			{
				if(random.nextInt(20)<1)
				{
					System.out.println("put "+nextByte+" to front");
					buf.putFront(nextByte);
					real.addFirst(nextByte);
				}
				else {
					System.out.println("put "+nextByte+" to end");
					buf.putByte(nextByte);
					real.addLast(nextByte);
				}
			}
			else {
				byte ans1=buf.getByte();
				byte ans2=real.pollFirst();
				if(ans1!=ans2||buf.size()!=real.size())
					throw new RuntimeException("what?");
				System.out.println("right "+ans1+" with size "+buf.size());
			}
		}
	}
}
public class NoelByteBuffer
{
	public static List<NoelByteBuffer> handlers;
	static {
		if(false)
			handlers=Collections.synchronizedList(new ArrayList<>());
	}
	byte shift;
	class NoelByteBufferBuffer
	{
		class NoelByteBufferBufferBuffer
		{
			byte[] data;
			int pos,size;
			NoelByteBufferBufferBuffer(byte[] bytes)
			{
				data=new byte[bytes.length];
				System.arraycopy(bytes,0,data,0,bytes.length);
				pos=0;
				size=bytes.length;
			}
			NoelByteBufferBufferBuffer(int size)
			{
				data=new byte[size];
				pos=0;
				this.size=0;
			}
			boolean full()
			{
				return size>=data.length;
			}
			byte getByte()
			{
				if(size()<=0)
					throw new IndexOutOfBoundsException("pos >= size");
				return data[pos++];
			}
			void putByte(byte b1)
			{
				if(full())
					throw new IndexOutOfBoundsException("array full");
				data[size++]=b1;
			}
			int size()
			{
				return size-pos;
			}
		}
		Deque<NoelByteBufferBufferBuffer> b;
		Stack<Byte> stack;
		int size;
		NoelByteBufferBuffer(byte[] bytes)
		{
			stack=new Stack<>();
			b=new LinkedList<>();
			size=bytes.length;
			b.addLast(new NoelByteBufferBufferBuffer(bytes));
		}
		int size()
		{
			return size;
		}
		void putByte(byte b1)
		{
			if(b.getLast()==null||b.getLast().full())
				b.addLast(new NoelByteBufferBufferBuffer(8192));
			b.getLast().putByte(b1);
			size++;
		}
		void putFront(byte b1)
		{
			stack.add(b1);
			size++;
		}
		byte getByte()
		{
			size--;
			if(!stack.empty())
				return stack.pop();
			while(b.getFirst().size()==0)
				b.pollFirst();
			return b.getFirst().getByte();
		}
	}
	NoelByteBufferBuffer buf;
	public NoelByteBuffer()
	{
		this(new byte[0]);
	}
	public NoelByteBuffer(byte[] bytes)
	{
		shift=0;
		if(handlers!=null)
			handlers.add(this);
		buf=new NoelByteBufferBuffer(bytes);
	}
	public int size()
	{
		return buf.size();
	}
	public byte getByte()
	{
		return buf.getByte();
	}
	public void putByte(byte b)
	{
		buf.putByte(b);
	}
	public void putFront(byte b)
	{
		buf.putFront(b);
	}

	public synchronized static void endAll()
	{
		if(handlers!=null)
		{
			handlers.forEach(buf -> buf.end());
			handlers.clear();
		}
	}
	public void end()
	{
		if(size()>0)
		{
			System.err.println("Warning: left "+size()+" byte(s)");
			List<Byte> l=new ArrayList<>();
			for(int i=0;size()>0&&i<32;i++)
				l.add(getByte());
			System.err.println("printing first "+l.size()+" bytes");
			for(Byte b:l)
				System.err.printf("%02X ",b);
			System.err.println();
			for(Byte b:l)
				System.err.printf("%s ",Character.isISOControl(b)?"..":" "+(char)b.byteValue());
			System.err.println();
//			data=null;
		}
	}
	public void addShift(byte add)
	{
		shift+=add;
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
	public void putUTFString(String s)
	{
		putShort((short) s.length());
		putBytes(s.getBytes(StandardCharsets.UTF_8));
	}
	public void putBoolean(boolean b)
	{
		putByte(b?(byte)1:(byte)0);
	}
	public void putSegment(NoelByteBuffer b)
	{
		putSegment(b.getAllBytes());
	}
	public void putSegment(byte[] data)
	{
		putInt(data.length);
		putBytes(data);
	}
	public String getString(int len)
	{
		return new String(getNBytes(len));
	}
}
