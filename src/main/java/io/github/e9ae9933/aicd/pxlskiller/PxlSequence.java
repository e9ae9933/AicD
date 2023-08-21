package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PxlSequence
{
	int width,height;
	int bodyX,bodyY;
	int shiftX,shiftY;
	int loopTo;
	String[] frameSnd;
	int aim;
	transient PxlFrame[] frames;
	byte useless;
	transient PxlPose fa;
	PxlSequence(NoelByteBuffer base,Settings s,int aim,PxlPose fa)
	{
		this.fa=fa;
		this.aim=aim;
		NoelByteBuffer b=base.getSegment();
		useless=b.getByte();
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
			frames[i]=new PxlFrame(base,s,this);
		}
	}
	public void output(NoelByteBuffer base,Settings s)
	{
		NoelByteBuffer b=new NoelByteBuffer();
		b.putByte(useless);
		b.putShort((short)width);
		b.putShort((short)height);
		b.putShort((short)bodyX);
		b.putShort((short)bodyY);
		b.putShort((short)shiftX);
		b.putShort((short)shiftY);
		b.putShort((short)loopTo);
		b.putShort((short)frameSnd.length);
		for (String string : frameSnd)
			b.putUTFString(string);
		base.putSegment(b);
		base.putShort((short)frames.length);
		for (PxlFrame frame : frames)
			frame.output(base,s);
	}
	public static PxlSequence breakDown(File dir,Settings s)
	{
		PxlSequence rt=s.gson.fromJson(new String(Utils.readAllBytes(new File(dir,"info.json")),StandardCharsets.UTF_8),PxlSequence.class);
		List<File> f=Arrays.stream(dir.listFiles()).filter(ff->!ff.getName().equals("info.json")).collect(Collectors.toList());
		f.sort(Comparator.comparingDouble(ff->Double.parseDouble(ff.getName().split("_")[1])));
		rt.frames=new PxlFrame[f.size()];
		for(int i=0;i<f.size();i++)
			rt.frames[i]=PxlFrame.breakDown(f.get(i),s);
		return rt;
	}
	public void export(File dir,Settings s) throws Exception
	{
		dir.mkdirs();
		FileOutputStream out=new FileOutputStream(new File(dir,"info.json"));
		out.write(s.gson.toJson(this).getBytes(StandardCharsets.UTF_8));
		out.close();
		for (int i = 0; i < frames.length; i++)
		{
			if(frames[i].layers.length!=1||true)
				frames[i].export(new File(dir, String.format("frame_%d_%s", i,frames[i].name)),s);
			else
				frames[i].exportSingleLayer(dir,i,s);
		}
	}
}
