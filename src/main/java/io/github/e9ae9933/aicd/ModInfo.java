package io.github.e9ae9933.aicd;

import java.io.File;
import java.net.URL;

public class ModInfo
{
	public String name;
	public File path;
	public URL url;
	public String version;
	public String author;

	public ModInfo(String name, File path)
	{
		this.name = name;
		this.path = path;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
