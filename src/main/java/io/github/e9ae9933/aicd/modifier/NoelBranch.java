package io.github.e9ae9933.aicd.modifier;

import java.util.Map;

public class NoelBranch extends NoelElement
{
	byte branch;
	NoelElement data;
	public NoelBranch(NoelByteBuffer b, Map<String,Object> settings, Map<String,Type> knownTypes)
	{
		branch=b.getByte();
		b.putFront(branch);
		Object o=settings.get(Byte.toString(branch));
//		System.out.println("branch "+branch+" found "+o);
		if(o==null)
			o=settings.get("default");
		String typeName=o.toString();
//		if(branch!=0)
//		{
//			System.out.println(branch+" "+typeName);
//			System.exit(-1);
//		}
		Type type=knownTypes.get(typeName);
		data=type.read(b,settings,knownTypes);
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		b.putByte(branch);
		data.writeTo(b);
	}
}
