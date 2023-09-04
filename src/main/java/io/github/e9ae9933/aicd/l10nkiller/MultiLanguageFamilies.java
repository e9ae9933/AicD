package io.github.e9ae9933.aicd.l10nkiller;

import java.util.concurrent.ConcurrentHashMap;

public class MultiLanguageFamilies extends ConcurrentHashMap<String,Family>
{
	MultiLanguageFamilies()
	{
		super();
	}
	public MultiLanguageFamilies diff(MultiLanguageFamilies master)
	{
		MultiLanguageFamilies rt=new MultiLanguageFamilies();
		master.forEach((k,v)->{
			if(this.containsKey(k))
			{
				Family f=this.get(k).diff(v);
				if(!f.isEmpty())
					rt.put(k,f);
			}
			else rt.put(k,v);
		});
		return rt;
	}
	public void merge(MultiLanguageFamilies o)
	{
		o.forEach((k,v)->{
			if(this.containsKey(k))
				this.get(k).merge(v);
			else this.put(k,v);
		});
	}
}
