package io.github.e9ae9933.aicd.l10nkiller;

import java.util.LinkedHashMap;

public class SingleEvent extends LinkedHashMap<String,Message>
{
	SingleEvent()
	{
		super();
	}
	void merge(SingleEvent o)
	{
		synchronized(this)
		{
			this.putAll(o);
		}
	}
	SingleEvent diff(SingleEvent master)
	{
		SingleEvent rt=new SingleEvent();
		master.forEach((k, v)->{
			if(!v.equals(this.get(k)))
				rt.put(k,v);
		});
		return rt;
	}
}
