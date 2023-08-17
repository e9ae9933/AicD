package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import java.awt.*;
import java.util.Map;

public class NoelPeek extends NoelElement
{
//	byte peek;
	boolean enabled=false;
	int len;
	NoelElement data;
	public NoelPeek(NoelByteBuffer b, Map<String,Object> settings, Map<String,Class<? extends NoelElement>> primitives, Map<String,NoelElement> variables)
	{
		if(settings==null)
			throw new IllegalArgumentException("peek must have settings");
		len=Integer.parseInt(settings.get("len").toString());
		byte[] peeks=new byte[len];
		for(int i=0;i<len;i++)
		{
			peeks[i] = b.getByte();
			if(peeks[i]!=0)
				enabled=true;
		}
		if(enabled)
		{
//			b.putFront(peek);
			for(int i=len-1;i>=0;i--)
				b.putFront(peeks[i]);
			data = NoelElement.newInstance(settings.get("value"), b, primitives, variables);
		}
		else
		{
			NoelZeroBuffer zero=new NoelZeroBuffer();
			data=NoelElement.newInstance(settings.get("value"),zero,primitives,variables);
		}
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		if(enabled)
		{
			data.writeTo(b);
		}
		else
			for(int i=0;i<len;i++)
				b.putByte((byte)0);
	}

	@Override
	public Component createGUI(Component parent)
	{
		return unsupportedGUI();
	}
}
