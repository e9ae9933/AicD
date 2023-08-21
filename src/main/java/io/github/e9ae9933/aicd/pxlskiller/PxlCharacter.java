package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PxlCharacter
{
	//List<PxlImage> images=new ArrayList<>();
	PxlPose[] poses;
	PxlImageAtlas[] atlases;
	PxlCharacter(File dir,Settings s)
	{
		s.target=this;
		List<File> f=Arrays.stream(dir.listFiles()).filter(ff->ff.isDirectory()).collect(Collectors.toList());
		//break them down
		f.sort(Comparator.comparingDouble(ff->breakDown(ff)));
		poses=new PxlPose[f.size()];
		for(int i=0;i<f.size();i++)
			poses[i]=PxlPose.breakDown(f.get(i),s);
		List<Pair<Integer,Double>> idList=new ArrayList<>();
		List<BufferedImage> imageList=new ArrayList<>();
		s.idImage.entrySet().stream().forEachOrdered(
				e->{
					idList.add(e.getKey());
					imageList.add(e.getValue());
				}
		);
		RectPackingAtlas[] packing=new RectPackingAtlas[1];
		List<Pair<Integer,Integer>> coords=RectPackingAtlas.getBestOne(imageList.stream().map(image->new Pair<>(image.getWidth(),image.getHeight())).collect(Collectors.toList()), packing);
		RectPackingAtlas atlas=packing[0];
		BufferedImage image=new BufferedImage(atlas.width,atlas.height,BufferedImage.TYPE_INT_ARGB);
		int n=s.idImage.size();
		Graphics2D g= (Graphics2D) image.getGraphics();
		for(int i=0;i<n;i++)
			g.drawImage(imageList.get(i),coords.get(i).first,coords.get(i).second,null);
		atlases=new PxlImageAtlas[1];
		PxlImageAtlas.Uv[] uv=new PxlImageAtlas.Uv[n];
		atlases[0]=new PxlImageAtlas(uv,image);
		for(int i=0;i<n;i++)
			uv[i]=new PxlImageAtlas.Uv(
					idList.get(i).first,
					idList.get(i).second,
					coords.get(i).first,
					coords.get(i).second,
					imageList.get(i).getWidth(),
					imageList.get(i).getHeight()
			);

	}
	double breakDown(File dir)
	{try
	{
		String s = dir.getName();
		return Double.parseDouble(s.split("_", 3)[1]);
	}
	catch (Exception e)
	{
		e.printStackTrace();
	}
	return -1;
	}
	PxlCharacter(NoelByteBuffer b,Settings s)
	{
		s.tasksToBeDone.clear();
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
					throw new IllegalArgumentException("not supported");
					/*
					//P_IMG
					int len = b.getInt();
					NoelByteBuffer target = new NoelByteBuffer(b.getNBytes(len));
					int n = target.getInt();
					for (int i = 0; i < n; i++)
						images.add(new PxlImage(target, s));
					break;*/
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
						poses[i]=new PxlPose(target,s,this);
					}
					break;
				}
				default:
					throw new IllegalArgumentException("invalid section header");
			}
		}
		s.dealWithTasks();
		b.end();
	}
	public NoelByteBuffer output(Settings s)
	{
		s.target=this;
		NoelByteBuffer b=new NoelByteBuffer();
		b.putInt(2000791807);
		b.putBytes("PXLS".getBytes(StandardCharsets.UTF_8));
		b.putBytes("%PACK_SECTION%".getBytes(StandardCharsets.UTF_8));
		NoelByteBuffer target1=new NoelByteBuffer();
		{
			target1.putInt(atlases.length);
			Arrays.stream(atlases).forEachOrdered(a -> target1.putBytes(a.output(s).getAllBytes()));
		}
		b.putSegment(target1);

		b.putBytes("%POSE_SECTION%".getBytes(StandardCharsets.UTF_8));
		NoelByteBuffer target2=new NoelByteBuffer();
		target2.putInt(poses.length);
		Arrays.stream(poses).forEachOrdered(p->p.output(target2,s));
		b.putSegment(target2);
		return b;
	}
	public byte[] outputAsBytes()
	{
		Settings s=new Settings();
		return output(s).getAllBytes();
	}
	public void export(File dir,Settings s) throws Exception
	{
		dir.mkdirs();
		Map<Pair<Integer,Double>,Pair<PxlImageAtlas, PxlImageAtlas.Uv>> map=new LinkedHashMap<>();
		Arrays.stream(atlases).forEach(a->{
			Arrays.stream(a.pos).forEach(p->{
				if(map.containsKey(new Pair<>(p.id,p.id2)))
					System.err.println("WHAT? found duplicate id "+p.id+" "+p.id2);
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
