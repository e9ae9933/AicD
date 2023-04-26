package io.github.e9ae9933.aicd;

import java.net.URL;

public class VersionInfo
{
	public String name;
	public URL url;
	public String source;

	public VersionInfo(String name, URL url,String source)
	{
		this.name = name;
		this.url = url;
		this.source=source;
	}
	@Override
	public String toString()
	{
		return "【"+source+"】 "+name;
	}
}
