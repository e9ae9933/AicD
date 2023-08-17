package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class PxlFrame
{
	int crf60;
	String name;
	transient PxlLayer[] layers;
	PxlFrame(NoelByteBuffer base,Settings s)
	{
		NoelByteBuffer b=base.getSegment();
		int vers=b.getUnsignedByte();
		crf60= b.getShort();
		name=b.getUTFString();
		if(b.size()!=0)
			System.err.println("left "+b.size()+" byte(s)");
		int num=base.getShort();
		layers=new PxlLayer[num];
		for(int i=0;i<num;i++)
		{
			layers[i]=new PxlLayer(base,s);
		}
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
		if(crf60!=10)
			joiner.add("crf"+crf60);
		if(name!=null&&!name.isEmpty())
			joiner.add('['+name+']');
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
