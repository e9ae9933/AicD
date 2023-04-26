package io.github.e9ae9933.aicd.modifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoelArrayEndsigned extends NoelElement
{
	List<NoelElement> data;
	public NoelArrayEndsigned(NoelByteBuffer b, Map<String,Object> settings,Map<String,Class<? extends NoelElement>> primitives,Map<String,NoelElement> variables)
	{
		Object o=settings.get("value");
		data =new ArrayList<>();
		for(int i=0;;i++)
		{
			byte sign1=b.getByte();
			byte sign2=b.getByte();
			if(sign1==0&&sign2==0)
				break;
			b.putFront(sign2);
			b.putFront(sign1);
			data.add(NoelElement.newInstance(o, b, primitives, variables));
		}
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		for(NoelElement e: data)
			e.writeTo(b);
		b.putShort((short) 0);
	}

	@Override
	public Component createGUI()
	{
		return unsupportedGUI();
	}
}
