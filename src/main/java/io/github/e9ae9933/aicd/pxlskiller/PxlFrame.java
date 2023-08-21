package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Utils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringJoiner;
import java.util.Vector;

public class PxlFrame
{
	int crf60=10;
	String name="";
	byte vers;
	transient PxlLayer[] layers;
	private PxlFrame()
	{

	}
	transient PxlSequence fa;
	PxlFrame(NoelByteBuffer base,Settings s,PxlSequence fa)
	{
		this.fa=fa;
		NoelByteBuffer b=base.getSegment();
		vers=b.getByte();
		crf60= b.getShort();
		name=b.getUTFString();
		if(b.size()!=0)
			System.err.println("left "+b.size()+" byte(s)");
		int num=base.getShort();
		layers=new PxlLayer[num];
		for(int i=0;i<num;i++)
		{
			layers[i]=new PxlLayer(base,s,this);
		}
	}
	public void output(NoelByteBuffer base,Settings s)
	{
		NoelByteBuffer b=new NoelByteBuffer();
		b.putByte(vers);
		b.putShort((short) crf60);
		b.putUTFString(name);
		base.putSegment(b);
		base.putShort((short) layers.length);
		for (PxlLayer layer : layers)
			layer.output(base,s);
	}
	public static PxlFrame breakDownDirectory(File dir,Settings s)
	{
		PxlFrame rt=s.gson.fromJson(new String(Utils.readAllBytes(new File(dir,"info.json")),StandardCharsets.UTF_8),PxlFrame.class);
		Vector<PxlLayer> l=new Vector<>();
		Arrays.stream(dir.listFiles()).filter(f->f.getName().endsWith(".png")&&!f.getName().endsWith("_NO_NEED_TO_MODIFY.png")||f.getName().endsWith(".json")&&!f.getName().equals("info.json")).sorted(Comparator.comparingDouble(f->Double.parseDouble(f.getName().split("_")[1])))
				.forEachOrdered(f->{
					String o=f.getName();
					if(o.endsWith(".json"))
						l.add(PxlLayer.constructFromJson(f,s));
					else
					{
						o = o.substring(0, o.length() - 4);
						o = o.split("_", 3)[2];
						l.add(new PxlLayer(o, f, s));
					}
				});
		rt.layers=l.toArray(new PxlLayer[0]);
		return rt;
	}
	public static PxlFrame breakDownFile(File file,Settings s)
	{
		if(true)
			throw new RuntimeException(file+" no file allowed now.");
		String str=file.getName();
		if(str.endsWith(".png"))
		{
			str=str.substring(0,str.length()-4);
			String frameName=str.split(",",2)[0];
			String layerName=str.split(",",2)[1];
			PxlFrame rt=new PxlFrame();
			int l=frameName.indexOf("["),r=frameName.lastIndexOf("]");
			if(l!=-1&&r!=-1)
				rt.name=frameName.substring(l+1,r);
			if(frameName.indexOf("crf",r)!=-1)
				rt.crf60=Integer.parseInt(frameName.substring(frameName.indexOf("crf",r)+3).split("_")[0]);
			if(!frameName.split("_",4)[3].equals(rt.describe()))
				System.err.println("WARNING: "+file+" not matches: read "+rt.describe()+" was "+frameName.split("_",4)[3]);
			rt.layers=new PxlLayer[1];
			rt.layers[0]=new PxlLayer(layerName,file,s);
			return rt;
		}
		else throw new RuntimeException("not png "+file);
	}
	public static PxlFrame breakDown(File file, Settings s)
	{
		if(file.isDirectory())
			return breakDownDirectory(file,s);
		else return breakDownFile(file,s);
	}

	public void export(File dir, Settings s) throws Exception
	{
		dir.mkdirs();
		FileOutputStream out=new FileOutputStream(new File(dir,"info.json"));
		out.write(s.gson.toJson(this).getBytes(StandardCharsets.UTF_8));
		out.close();
		for (int i = 0; i < layers.length; i++)
		{
			layers[i].export(dir,i,s);
		}
	}
	String describe()
	{
		StringJoiner joiner=new StringJoiner("_");
		if(name!=null&&!name.isEmpty())
			joiner.add('['+name+']');
		if(crf60!=10)
			joiner.add("crf"+crf60);
		return joiner.toString();
	}
	public void exportSingleLayer(File dir,int idl,Settings s) throws Exception
	{
		String fileName= String.format("frame_%d_single_%s,%s",idl, describe(),layers[0].describe());
		FileOutputStream fos=new FileOutputStream(new File(dir,fileName+".png"));
		ImageIO.write(layers[0].getImage(s),"png",fos);
		fos.close();

	}
}
