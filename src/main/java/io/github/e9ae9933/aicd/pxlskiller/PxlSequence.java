package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class PxlSequence
{
	int width,height;
	int bodyX,bodyY;
	int shiftX,shiftY;
	int loopTo;
	String[] frameSnd;
	transient PxlFrame[] frames;
	PxlSequence(NoelByteBuffer base,Settings s)
	{
		NoelByteBuffer b=base.getSegment();
		b.getByte();
		width=b.getUnsignedShort();
		height=b.getUnsignedShort();
		bodyX=b.getShort();
		bodyY=b.getShort();
		shiftX=b.getShort();
		shiftY=b.getShort();
		loopTo=b.getShort();
		int num=b.getUnsignedShort();
		frameSnd=new String[num];
		for(int i=0;i<num;i++)
			frameSnd[i]=b.getUTFString();
		int num2=base.getUnsignedShort();
		int num3=0;
		frames=new PxlFrame[num2];
		for(int i=0;i<num2;i++)
		{
			frames[i]=new PxlFrame(base,s);
		}
	}
	public void export(File dir,Settings s) throws Exception
	{
		dir.mkdirs();
		FileOutputStream out=new FileOutputStream(new File(dir,"info.json"));
		out.write(s.gson.toJson(this).getBytes(StandardCharsets.UTF_8));
		out.close();
		for (int i = 0; i < frames.length; i++)
		{
			if(frames[i].layers.length!=1)
				frames[i].export(new File(dir, String.format("frame_%d_%s", i,frames[i].name)),s);
			else
				frames[i].exportSingleLayer(dir,i,s);
		}
	}
}
