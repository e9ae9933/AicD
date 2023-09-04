package io.github.e9ae9933.aicd.l10nkiller;

import java.util.concurrent.ConcurrentHashMap;

public class Family extends ConcurrentHashMap<String,SingleEvent>
{
	Family()
	{
		super();
	}
	void merge(Family o)
	{
		//todo: assumed texts really short
		synchronized (this)
		{
			o.forEach((s, e) ->
			{
				if (this.containsKey(s))
					this.get(s).merge(e);
				else this.put(s, e);
			});
		}
	}
	Family diff(Family master)
	{
		Family rt=new Family();
		master.forEach((k, v)->{
			if(this.containsKey(k))
			{
				SingleEvent se=this.get(k).diff(v);
				if(!se.isEmpty())
					rt.put(k,se);
			}
			else rt.put(k,v);
		});
		return rt;
	}
}
