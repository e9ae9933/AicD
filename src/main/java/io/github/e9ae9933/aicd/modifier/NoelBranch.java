package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import java.awt.*;
import java.util.Map;

public class NoelBranch extends NoelElement
{
	byte branch;
	NoelElement data;
	public NoelBranch(NoelByteBuffer b, Map<String,Object> settings, Map<String,Class<? extends NoelElement>> primitives, Map<String,NoelElement> variables)
	{
		branch=b.getByte();
		b.putFront(branch);
		Object o=settings.get(Byte.toString(branch));
		if(o==null)
			o=settings.get("default");
		data=NoelElement.newInstance(o,b,primitives,variables);
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putByte(branch);
		data.writeTo(b);
	}

	@Override
	public Component createGUI(Component parent)
	{
		return unsupportedGUI();
	}
}
