package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PxlPose
{
	boolean autoFlip;
	boolean tetraPose;
	String title;
	int width,height;
	int endJumpLoopCount;
	String endJumpTitle;
	String[] aliasTo;
	String comment;
	transient PxlSequence[] sequences;
	PxlPose(NoelByteBuffer b,Settings s)
	{
		NoelByteBuffer target=b.getSegment();
		int num=target.getByte();
		autoFlip=target.getBoolean();
		tetraPose=target.getBoolean();
		title=target.getUTFString();
		width=target.getShort();
		height=target.getShort();
		endJumpLoopCount=target.getShort();
		endJumpTitle=target.getUTFString();
		int num2=target.getShort();
		aliasTo=new String[num2];
		//for(int i=0;i<num2;i++)
		for(int i=0;i<num2;i++)
			aliasTo[i]=target.getUTFString();
		if(num>=2)
			comment=target.getUTFString();
		int num3;
		List<PxlSequence> list=new ArrayList<>();
		while((num3=Byte.toUnsignedInt(b.getByte()))!=0)
		{
			num3-=10;
			//System.out.println("num3 "+num3);
			list.add(new PxlSequence(b,s));
		}
		sequences=list.toArray(new PxlSequence[0]);
	}
	public void export(File dir,Settings s) throws Exception
	{
		dir.mkdirs();
		FileOutputStream out=new FileOutputStream(new File(dir,"info.json"));
		out.write(s.gson.toJson(this).getBytes(StandardCharsets.UTF_8));
		out.close();
		for (int i = 0; i < sequences.length; i++)
		{
			sequences[i].export(new File(dir, String.format("sequence_%d_%s", i,"")),s);
		}
	}
}
