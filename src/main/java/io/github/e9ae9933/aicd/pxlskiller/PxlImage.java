package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class PxlImage
{
	int type;
	int id;
	double id2;
	BufferedImage I,P;
	PxlImage(NoelByteBuffer b,Settings s)
	{
		if(true)
			throw new IllegalArgumentException("imgs");
		type=b.getByte()-22;
		if(type<0)
			return;
		b.getByte();
		id=b.getInt();
		id2=b.getDouble();
		if(type==0||type==8)
		{
			NoelByteBuffer target=b.getSegment();
			if(target.size()>0)
				I=getImage(target);
			NoelByteBuffer target2=b.getSegment();
			if(target2.size()>0)
				P=getImage(target2);
		}
	}
	BufferedImage getImage(NoelByteBuffer b)
	{/*
		if(b.getInt()!=(int)2303741511L) throw new IllegalArgumentException("bad sign");
		if(b.getInt()!=218765834) throw new IllegalArgumentException("bad sign 2");
		b.getInt();
		if(!b.getString(4).equals("IHDR")) throw new IllegalArgumentException("bad sign 3");*/
		try
		{
			return ImageIO.read(new ByteArrayInputStream(b.getNBytes(b.size())));
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("error image",e);
		}
	}
}
