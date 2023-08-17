package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.StringJoiner;

public class PxlLayer
{
	transient int id;
	transient double id2;
	byte type;
	String name;
	short alpha;
	float x,y;
	double zmx,zmy;
	double rotR;
	int blendVariable;
	PxlLayer(NoelByteBuffer b,Settings s)
	{
		id=b.getInt();
		id2=b.getDouble();
		type=b.getByte();
		//get image
		name=b.getUTFString();
		alpha=b.getShort();
		x=b.getShort()/10f;
		y=b.getShort()/10f;
		zmx=b.getDouble();
		zmy=b.getDouble();
		rotR=b.getDouble();
		blendVariable=b.getUnsignedShort();
		b.getInt();
		b.getByte();
		b.getByte();


	}
	public boolean available(String name)
	{
		for (char c : name.toCharArray())
		{
			if(c>=0&&c<=31)return false;
			if("<>:\"/\\|?*".contains(c+""))return false;
			if(",[]".contains(c+""))return false;
		}
		return true;
	}
	public String describe()
	{
		StringJoiner joiner=new StringJoiner("_");
		if(name!=null&&!name.isEmpty())
		{
			if(available(name))
				joiner.add('[' + name + ']');
			else
				joiner.add("[["+ Base64.getUrlEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8))+"]]");
		}
		if(type!=0)
			joiner.add("type"+type);
		if(alpha!=10000)
			joiner.add("alpha"+alpha);
		joiner.add('('+Float.toString(x)+','+y+')');
		if(zmx!=1||zmy!=1)
			joiner.add(String.format("(%s,%s)", zmx,zmy));
		if(rotR!=0)
			joiner.add("rot"+rotR);
		if(blendVariable!=0)
			joiner.add("blend"+blendVariable);
		return joiner.toString();
	}

	public void export(File dir,int idl, Settings s) throws Exception
	{/*
		String name= String.format("layer_%d_[%s]_type%d_alpha%d_(%s,%s)_(%s,%s)_rot%s_blend%d",
				idl,this.name,type,alpha,x+"",y+"",zmx+"",zmy+"",rotR+"",blendVariable);*/
		String name="layer_"+idl+"_"+describe();
		FileOutputStream fos=new FileOutputStream(new File(dir,name+".png"));
		ImageIO.write(getImage(s),"png",fos);
		fos.close();

	}
	public BufferedImage getImage(Settings s)
	{
		Pair<PxlImageAtlas, PxlImageAtlas.Uv> pr=s.idMap.get(new Pair<>(id,id2));
		if(pr==null)
		{
			throw new RuntimeException("Unfounded image for "+name);
		}
		BufferedImage img=pr.first.image;
		PxlImageAtlas.Uv u=pr.second;
		try
		{
			BufferedImage sub = img.getSubimage(u.x, u.y, u.width, u.height);
			return sub;
		}
		catch (Exception e)
		{
			String msg=String.format("Failed: (%d,%d) (%d,%d)", u.x,u.y,u.width,u.height);
			System.out.println(msg);
			e.printStackTrace();
			BufferedImage image=new BufferedImage(u.width,u.height,BufferedImage.TYPE_INT_ARGB);
			Graphics graphics=image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0,0,u.width,u.height);
			graphics.setColor(Color.BLACK);
			graphics.drawString(msg,0,10);
			return image;
			//throw e;
		}
	}
}
