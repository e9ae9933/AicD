package io.github.e9ae9933.aicd.modifier;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

public class NoelDynamicArray extends NoelArray
{
	NoelLength len;
	public NoelDynamicArray(NoelByteBuffer b, Map<String,Object> settings, Map<String,Class<? extends NoelElement>> primitives, Map<String,NoelElement> variables)
	{
		len= (NoelLength) variables.get(settings.get("len").toString());
		long length=len.get();
		Consumer<NoelDynamicArray> updater=(a)->{
			len.set(a.data.size());
//			System.out.println("set "+a.data.size());
		};
		data=new ArrayList<NoelElement>()
		{
			@Override
			public boolean add(NoelElement noelElement)
			{
				boolean rt=super.add(noelElement);
				updater.accept(NoelDynamicArray.this);
				return rt;
			}

			@Override
			public void add(int index, NoelElement element)
			{
				super.add(index, element);
				updater.accept(NoelDynamicArray.this);
			}

			@Override
			public NoelElement remove(int index)
			{
				NoelElement rt=super.remove(index);
				updater.accept(NoelDynamicArray.this);
				return rt;
			}

			@Override
			public void clear()
			{
				super.clear();
				updater.accept(NoelDynamicArray.this);
			}
		};
		Object o=settings.get("value");
		valueType=o;
		this.primitives=primitives;
		this.variables=variables;
		for(int i=0;i<length;i++)
			data.add(NoelElement.newInstance(o,b,primitives,variables));
		updater.accept(this);
	}
	@Override
	public void writeTo(NoelByteBuffer b)
	{
		long length= len.get();
		for(int i=0;i<length;i++)
			data.get(i).writeTo(b);
	}
}
