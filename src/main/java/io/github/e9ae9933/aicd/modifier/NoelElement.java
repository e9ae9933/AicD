package io.github.e9ae9933.aicd.modifier;

import java.lang.reflect.Constructor;
import java.util.Map;

public abstract class NoelElement
{
	public String objectType=getClass().getCanonicalName();
	public abstract void writeTo(NoelByteBuffer b);
	public static NoelElement newInstance(Class<? extends NoelElement> clazz,NoelByteBuffer b, Map<String,Object> settings,Map<String,Type> knownTypes)
	{
		Constructor<? extends NoelElement> cons;
		try
		{
			cons=clazz.getDeclaredConstructor(NoelByteBuffer.class,Map.class,Map.class);
			if(cons==null)
				throw new NoSuchMethodException();
			return cons.newInstance(b,settings,knownTypes);
		}
		catch (NoSuchMethodException ignored)
		{
			try
			{
				cons = clazz.getDeclaredConstructor(NoelByteBuffer.class);
				return cons.newInstance(b);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		catch (Exception e)
		{
//			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
	}
}
