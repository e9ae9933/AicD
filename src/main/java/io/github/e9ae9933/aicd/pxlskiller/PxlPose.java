package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
	byte useless;
	transient PxlCharacter fa;
	PxlPose(NoelByteBuffer b,Settings s,PxlCharacter fa)
	{
		this.fa=fa;
		NoelByteBuffer target=b.getSegment();
		useless=target.getByte();
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
		if(useless>=2)
			comment=target.getUTFString();
		int num3;
		List<PxlSequence> list=new ArrayList<>();
		while((num3=Byte.toUnsignedInt(b.getByte()))!=0)
		{
			num3-=10;
			//System.out.println("num3 "+num3);
			list.add(new PxlSequence(b,s,num3,this));
		}
		sequences=list.toArray(new PxlSequence[0]);
	}
	void output(NoelByteBuffer b,Settings s)
	{
		NoelByteBuffer target=new NoelByteBuffer();
		target.putByte(useless);
		target.putBoolean(autoFlip);
		target.putBoolean(tetraPose);
		target.putUTFString(title);
		target.putShort((short) width);
		target.putShort((short) height);
		target.putShort((short) endJumpLoopCount);
		target.putUTFString(endJumpTitle);
		target.putShort((short) aliasTo.length);
		Arrays.stream(aliasTo).forEachOrdered(a->target.putUTFString(a));
		if(useless>=2)
			target.putUTFString(comment);
		b.putSegment(target);
		for (PxlSequence sequence : sequences)
		{
			b.putByte((byte) (sequence.aim+10));
			sequence.output(b,s);
		}
		b.putByte((byte) 0);
	}
	static PxlPose breakDown(File dir,Settings s)
	{
		PxlPose rt=s.gson.fromJson(new String(Utils.readAllBytes(new File(dir,"info.json")),StandardCharsets.UTF_8),PxlPose.class);
		List<File> f= Arrays.stream(dir.listFiles()).filter(ff->ff.isDirectory()).collect(Collectors.toList());
		//break them down
		f.sort(Comparator.comparingDouble(ff->Double.parseDouble(ff.getName().split("_", 3)[1])));
		rt.sequences=new PxlSequence[f.size()];
		for(int i=0;i<f.size();i++)
			rt.sequences[i]=PxlSequence.breakDown(f.get(i),s);
		return rt;
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
