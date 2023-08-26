package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Pair;
import io.github.e9ae9933.aicd.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class PxlLayer
{
	transient int id;
	transient double id2;
	byte type=0;
	String name="";
	short alpha=10000;
	float x,y;
	double zmx=1,zmy=1;
	double rotR=0;
	int blendVariable=0;
	int useless1;
	byte useless2,useless3;
	String referenceLayer,referenceFrame,referenceSequence,referencePose;
	int referenceLayerFallback=-1,referenceFrameFallback=-1,referenceSequenceFallback=-1,referencePoseFallback=-1;
	transient BufferedImage image;
	transient boolean isImport=false;
	static PxlLayer constructFromJson(File file,Settings s)
	{
		if(!file.getName().endsWith(".json"))
			throw new RuntimeException(file+" not json");
		FileInputStream fis=Utils.ignoreExceptions(()->new FileInputStream(file));
		byte[] b=Utils.readAllBytes(fis);
		Utils.ignoreExceptions(()->fis.close());
		PxlLayer layer=s.gson.fromJson(new String(b,StandardCharsets.UTF_8),PxlLayer.class);
		layer.isImport=true;
		if(layer.referenceLayerFallback==-1)
		{
			System.out.println("WARNING: "+file+" -1");
		}
		return layer;
	}
	PxlLayer(String desc,File file,Settings s)
	{
		if(!file.getName().endsWith(".png"))
			throw new RuntimeException(file+" not png");
		id=s.random.nextInt(1048576);
		id2=s.random.nextInt(16777216);
		try
		{
			image = ImageIO.read(file);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		s.idImage.put(new Pair<>(id,id2),image);
		String original=desc;
		if(desc.startsWith("[["))
		{
			int r=desc.lastIndexOf("]]");
			String b64=desc.substring(2,r);
			name=new String(Base64.getUrlDecoder().decode(b64),StandardCharsets.UTF_8);
			desc=desc.substring(r+2);
		}
		else if(desc.startsWith("["))
		{
			int r=desc.lastIndexOf("]");
			String notb64=desc.substring(1,r);
			name=notb64;
			desc=desc.substring(r+1);
		}
		if(desc.startsWith("_"))
			desc=desc.substring(1);
		boolean xy=false;
		for(String str:desc.split("_"))
		{
			if(str.startsWith("type"))
				type=Byte.parseByte(str.substring(4));
			if(str.startsWith("alpha"))
				alpha=Short.parseShort(str.substring(5));
			if(str.startsWith("(")&&str.endsWith(")"))
			{
				String to=str.substring(1,str.length()-1);
				double i=Double.parseDouble(to.split(",")[0]);
				double j=Double.parseDouble(to.split(",")[1]);
				if(!xy)
				{
					x= (float) i;
					y= (float) j;
					xy=true;
				}
				else {
					zmx=i;
					zmy=j;
				}
			}
			if(str.startsWith("rot"))
				rotR=Double.parseDouble(str.substring(3));
			if(str.startsWith("blend"))
				blendVariable=Integer.parseInt(str.substring(5));
			if(str.startsWith("ua"))
				useless1=Integer.parseInt(str.substring(2));
			if(str.startsWith("ub"))
				useless2=Byte.parseByte(str.substring(2));
			if(str.startsWith("uc"))
				useless3=Byte.parseByte(str.substring(2));

		}
		if(!original.equals(describe()))
			System.err.println("WARNING: layer "+original+" decoded into "+describe());
	}
	transient PxlFrame fa;
	PxlLayer(NoelByteBuffer b,Settings s,PxlFrame fa)
	{
		this.fa=fa;
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
		useless1=b.getInt();
		useless2=b.getByte();
		useless3=b.getByte();
		if((type&1)==1)
		{
			s.tasksToBeDone.add(this::calculateReference);
		}
		else {
			s.referenceMap.put(new Pair<>(id,id2),this);
		}


	}
	void calculateReference(Settings s)
	{
		if((type&1)==0)
			throw new IllegalArgumentException("no not 1 allowed");
		PxlLayer layer=s.referenceMap.get(new Pair<>(id,id2));
		if(layer==null)
		{
			//System.out.println("WARNING: layer null: " + id + " " + id2 + " for " + name);
			s.referenceMap.put(new Pair<>(id,id2),this);
			//to not do: is it correct?
			//type^=1;
			isImport=false;
			return;
		}
		isImport=true;
		//System.out.println("found "+id+" "+id2+" "+name);
		PxlFrame frame=layer.fa;
		PxlSequence sequence=frame.fa;
		PxlPose pose=sequence.fa;
		PxlCharacter character=pose.fa;
		referenceLayer=layer.name;
		referenceLayerFallback=find(frame.layers, layer);
		referenceFrame=frame.name;
		referenceFrameFallback=find(sequence.frames, frame);
		referenceSequence=sequence.aim+"";
		referenceSequenceFallback=find(pose.sequences, sequence);
		referencePose=pose.title;
		referencePoseFallback=find(character.poses,pose);
	}
	<T> int find(T[] arr,T o)
	{
		for(int i=0;i<arr.length;i++)
			if(o.equals(arr[i]))
				return i;
		System.out.println("WARNING: find -1");
		return -1;
	}
	public void output(NoelByteBuffer b,Settings s)
	{
		if(isImport)
		{
			PxlCharacter chara=s.target;
			List<PxlPose> poseList=Arrays.stream(chara.poses).filter(p->p.title.equals(referencePose)).collect(Collectors.toList());
			PxlPose pose=poseList.size()==1?poseList.get(0):chara.poses[referencePoseFallback];
			List<PxlSequence> sequenceList= Arrays.stream(pose.sequences).filter(seq->(seq.aim+"").equals(referenceSequence)).collect(Collectors.toList());
			PxlSequence sequence=sequenceList.size()==1?sequenceList.get(0):pose.sequences[referenceSequenceFallback];
			List<PxlFrame> frameList= Arrays.stream(sequence.frames).filter(f->f.name.equals(referenceFrame)).collect(Collectors.toList());
			PxlFrame frame=frameList.size()==1?frameList.get(0):sequence.frames[referenceFrameFallback];
			List<PxlLayer> layerList= Arrays.stream(frame.layers).filter(l->l.name.equals(referenceLayer)).collect(Collectors.toList());
			PxlLayer layer=layerList.size()==1?layerList.get(0):frame.layers[referenceLayerFallback];
			id=layer.id;
			id2=layer.id2;
		}
		b.putInt(id);
		b.putDouble(id2);
		b.putByte(type);
		b.putUTFString(name);
		b.putShort(alpha);
		b.putShort((short)Math.round(x*10f));
		b.putShort((short)Math.round(y*10f));
		b.putDouble(zmx);
		b.putDouble(zmy);
		b.putDouble(rotR);
		b.putShort((short) blendVariable);
		b.putInt(useless1);
		b.putByte((byte)useless2);
		b.putByte((byte)useless3);

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
		if(isImport)
			throw new RuntimeException("no type 1 is allowed");
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
		if(useless1!=0)
			joiner.add("ua"+useless1);
		if(useless2!=0)
			joiner.add("ub"+useless2);
		if(useless3!=0)
			joiner.add("uc"+useless3);
		return joiner.toString();
	}

	public void export(File dir,int idl, Settings s) throws Exception
	{
		if(!isImport)
		{
			String name = "layer_" + idl + "_" + describe();
			FileOutputStream fos = new FileOutputStream(new File(dir, name + ".png"));
			BufferedOutputStream bos=new BufferedOutputStream(fos);
			ImageIO.write(getImage(s), "png", bos);
			bos.close();
			fos.close();
		}
		else {
			String name="layer_"+idl+"_"+(available(this.name)?"["+this.name+"]":"[["+Base64.getUrlEncoder().encodeToString(this.name.getBytes(StandardCharsets.UTF_8))+"]]");
			FileOutputStream fos=new FileOutputStream(new File(dir,name+".json"));
			if(this.referencePoseFallback==-1)
				System.out.println("WARNING: "+" why -1 "+dir+" "+name);
			fos.write(s.gson.toJson(this).getBytes(StandardCharsets.UTF_8));
			fos.close();
			if(s.writeExtra)
			{
				BufferedImage img=getImage(s);
				FileOutputStream fos2 = new FileOutputStream(new File(dir, name + "_" + img.getWidth() + "," + img.getHeight() + "_NO_NEED_TO_MODIFY" + ".png"));
				ImageIO.write(img, "png", fos2);
				fos2.close();
			}
		}

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
