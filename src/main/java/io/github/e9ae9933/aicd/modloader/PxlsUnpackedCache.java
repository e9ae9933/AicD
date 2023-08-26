package io.github.e9ae9933.aicd.modloader;

import io.github.e9ae9933.aicd.Policy;
import io.github.e9ae9933.aicd.Utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PxlsUnpackedCache implements FileUtils
{
	ConcurrentHashMap<String,String> originalMD5;
	PxlsUnpackedCache(File dir)
	{
		originalMD5=new ConcurrentHashMap<>();
		Arrays.stream(dir.listFiles())
				.parallel()
				.filter(f->f.isDirectory())
				.forEach(f->{
					originalMD5.put(f.getName(),md5Dir(f));
				});
	}
//	void printTo(File file)
//	{
//		Utils.writeAllUTFString(file, Policy.gson.toJson(originalMD5));
//	}
//	static PxlsUnpackedCache readFrom(File file)
//	{
//		return Policy.gson.fromJson(new String(Utils.readAllBytes(file), StandardCharsets.UTF_8),PxlsUnpackedCache.class);
//	}
	List<File> unmatch(File dir)
	{
		return Arrays.stream(dir.listFiles())
				.parallel()
				.filter(f->f.isDirectory())
				.filter(f->!md5Dir(f).equals(originalMD5.get(f.getName())))
				.collect(Collectors.toList());
	}
}
