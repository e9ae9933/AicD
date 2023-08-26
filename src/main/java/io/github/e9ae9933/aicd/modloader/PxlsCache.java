package io.github.e9ae9933.aicd.modloader;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PxlsCache implements FileUtils
{
	ConcurrentHashMap<String,String> originalMD5;
	PxlsCache(File dir)
	{
		originalMD5=new ConcurrentHashMap<>();
		Arrays.stream(dir.listFiles()).parallel().filter(f->f.isFile()).forEach(f->{
			originalMD5.put(f.getName(),md5(f));
		});
	}
	public List<File> getChangedFiles(File dir)
	{
		return Arrays.stream(dir.listFiles()).parallel()
				.filter(f->f.isFile())
				.filter(f-> !originalMD5.containsKey(f.getName())||
					!originalMD5.get(f.getName()).equalsIgnoreCase(md5(f)))
				.collect(Collectors.toList());
	}
}
