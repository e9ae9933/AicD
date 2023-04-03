package io.github.e9ae9933.aicd.modifier;

import java.util.Map;

public class NoelSettingsShift extends NoelElement
{
	byte shift;
	public NoelSettingsShift(NoelByteBuffer b, Map<String,Object> settings,Map<String,Type> knownTypes)
	{
		b.addShift(shift=Byte.parseByte(settings.get("shift").toString()));
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.addShift(shift);
	}
}
