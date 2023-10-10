package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.Constants;
import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Pair;
import io.github.e9ae9933.aicd.Policy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class PxlCharacter
{
	//List<PxlImage> images=new ArrayList<>();
	PxlPose[] poses;
	PxlImageAtlas[] atlases;
	public PxlCharacter(File dir,Settings s)
	{
		if(!Constants.shouldWeHandlePxls)
			return;
		s.target=this;
		List<File> f=Arrays.stream(dir.listFiles()).filter(ff->ff.isDirectory()).collect(Collectors.toList());
		//break them down
		f.sort(Comparator.comparing(ff->ff.getName()));
		poses=new PxlPose[f.size()];
		for(int i=0;i<f.size();i++)
			poses[i]=PxlPose.breakDown(f.get(i),s);
		Arrays.sort(poses,Comparator.comparingDouble(p->p.priority));
		build(dir,s);
	}
	private void build(File dir,Settings s)
	{

		List<Pair<Integer,Double>> idList=new ArrayList<>();
		List<BufferedImage> imageList=new ArrayList<>();
		s.idImage.entrySet().stream().forEachOrdered(
				e->{
					idList.add(e.getKey());
					imageList.add(e.getValue());
				}
		);
		idList.add(new Pair<>(-1,-1.0));

		{
//			String json=Policy.getGson().toJson(this);
			int w=640,h=360/2;
			BufferedImage bi=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
			Graphics2D g=bi.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
			g.setColor(Color.WHITE);
			g.fillRect(0,0,w,h);
			g.setColor(Color.BLACK);
			g.fillRect(8,8,w-8-8,h-8-8);
			g.setFont(Constants.unifont.deriveFont(16.0f));
			g.setColor(Color.WHITE);
			StringJoiner sj=new StringJoiner("\n");
			sj.add("AicD "+Constants.version+" "+Constants.versionCode);
			sj.add("java "+System.getProperty("java.runtime.version"));
			sj.add("name "+dir.getName());
			sj.add("size "+s.idImage.size());
			sj.add("with "+poses.length+" poses");
//			sj.add("json length "+json.length());
			int y=32;
			for(String ss:sj.toString().split("\n"))
			{
				g.drawString(ss,32,y);
				y+=16;
			}
			imageList.add(bi);
		}

		RectPackingAtlas[] packing=new RectPackingAtlas[1];
		List<Pair<Integer,Integer>> coords=RectPackingAtlas.getBestOne(imageList.stream().map(image->new Pair<>(image.getWidth(),image.getHeight())).collect(Collectors.toList()), packing);
		RectPackingAtlas atlas=packing[0];
		BufferedImage image=new BufferedImage(atlas.width,atlas.height,BufferedImage.TYPE_INT_ARGB);
		int n=idList.size();
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
		try(ByteArrayOutputStream baos=new ByteArrayOutputStream()){
			ImageIO.write(image,"png",baos);
			s.exportPng=baos.toByteArray();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

	}
//	double breakDown(File dir)
//	{try
//	{
//		String s = dir.getName();
//		return Double.parseDouble(s.split("_", 3)[1]);
//	}
//	catch (Exception e)
//	{
//		e.printStackTrace();
//	}
//	return -1;
//	}
	public PxlCharacter(NoelByteBuffer b, Settings s)
	{
		if(!Constants.shouldWeHandlePxls)
			return;
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
						poses[i]=new PxlPose(target,s,i,this);
					}
					Arrays.sort(poses,Comparator.comparingDouble(p->p.priority));
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
	public byte[] outputAsBytes(Settings s)
	{
		return output(s).getAllBytes();
	}
	public void export(File dir,Settings s) throws Exception
	{
		if(!Constants.shouldWeHandlePxls)
			return;
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
//		Arrays.sort(poses,Comparator.comparingDouble(p->p.priority));
		for (int i = 0; i < poses.length; i++)
		{
			poses[i].export(new File(dir, String.format("pose_%s",poses[i].title)),s);
		}
	}
}
