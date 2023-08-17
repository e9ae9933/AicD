package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Pair;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PxlCharacter
{
	List<PxlImage> images=new ArrayList<>();
	PxlPose[] poses;
	PxlImageAtlas[] atlases;
	PxlCharacter(NoelByteBuffer b,Settings s)
	{
		// header
		if(b.getInt()!=2000791807)
			throw new IllegalArgumentException("invalid header");
		if(!b.getString(4).equals("PXLS"))
			throw new IllegalArgumentException("invalid header");
		while(b.size()>0)
		{
			if(b.size()<14)
			{
				break;
			}
			String op=new String(b.getNBytes(14), StandardCharsets.UTF_8);
			//System.out.printf("Section header %s\n",op);
			switch (op)
			{
				case "%IMGD_SECTION%":
					throw new IllegalArgumentException("does not support for compressed image data");
				case "%IMGS_SECTION%":
				{
					//P_IMG
					int len = b.getInt();
					NoelByteBuffer target = new NoelByteBuffer(b.getNBytes(len));
					int n = target.getInt();
					for (int i = 0; i < n; i++)
						images.add(new PxlImage(target, s));
					break;
				}
				case "%PACK_SECTION%":
				{
					//P_IMG_PACKED
					int len = b.getInt();
					NoelByteBuffer target = new NoelByteBuffer(b.getNBytes(len));
					int n = target.getInt();
					atlases=new PxlImageAtlas[n];
					for (int i = 0; i < n; i++)
					{
						atlases[i]=new PxlImageAtlas(target,s,i);
					}
					break;
				}
				case "%IMGV_SECTION%":
				{
					NoelByteBuffer target = b.getSegment();
					System.out.println("Ignored segment IMGV with byte(s) " + target.size());
					target.getAllBytes();
					break;
				}
				case "%POSE_SECTION%":
				{
					NoelByteBuffer target = b.getSegment();
					int n = target.getInt();
					//System.out.println("Found "+n+" pose(s)");
					poses=new PxlPose[n];
					for(int i=0;i<n;i++)
					{
						//System.out.println("Reading pose "+i);
						poses[i]=new PxlPose(target,s);
					}
					break;
				}
				default:
					throw new IllegalArgumentException("invalid section header");
			}
		}
		b.end();
	}
	public void export(File dir,Settings s) throws Exception
	{
		dir.mkdirs();
		Map<Pair<Integer,Double>,Pair<PxlImageAtlas, PxlImageAtlas.Uv>> map=new LinkedHashMap<>();
		Arrays.stream(atlases).forEach(a->{
			Arrays.stream(a.pos).forEach(p->{
				map.put(new Pair<>(p.id,p.id2),new Pair<>(a,p));
			});
		});
		s.idMap=map;
		for (int i = 0; i < poses.length; i++)
		{
			poses[i].export(new File(dir, String.format("pose_%d_%s", i,poses[i].title)),s);
		}
	}
}
