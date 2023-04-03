package io.github.e9ae9933.aicd;

import java.net.URL;

public class VersionInfo
{
	public String name;
	public URL url;

	public VersionInfo(String name, URL url)
	{
		this.name = name;
		this.url = url;
	}
	@Override
	public String toString()
	{
		return name;
	}
}
