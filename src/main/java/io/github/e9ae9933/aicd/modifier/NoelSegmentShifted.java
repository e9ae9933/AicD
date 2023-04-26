package io.github.e9ae9933.aicd.modifier;

import java.util.Map;

public class NoelSegmentShifted extends NoelElement
{
	NoelElement data;
	byte shift;
	public NoelSegmentShifted(NoelByteBuffer b, Map<String,Object> settings,Map<String,Class<? extends NoelElement>> primitives,Map<String,NoelElement> variables)
	{
		if(settings==null)
			throw new IllegalArgumentException("Segment must have settings");
		int len=b.getInt();
		shift=b.getByte();
		Object o=settings.get("value");
		NoelByteBuffer buf=new NoelByteBuffer(b.getNBytes(len));
		buf.addShift(shift);
		data=NoelElement.newInstance(o,buf,primitives,variables);
		if(buf.size()!=0)
			System.err.println("Warning: left "+buf.size()+" bytes");
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		NoelByteBuffer buf=new NoelByteBuffer();
		data.writeTo(buf);
		buf.addShift(shift);
		b.putInt(buf.size());
		b.putBytes(buf.getNBytes(buf.size()));
	}
}
