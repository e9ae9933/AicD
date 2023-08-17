package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class PxlImageAtlas
{
	static class Uv
	{
		int id;
		double id2;
		int x,y,width,height;
	}
	Uv[] pos;
	BufferedImage image;
	PxlImageAtlas(NoelByteBuffer b,Settings s,int id)
	{
		int type=b.getByte()-22;
		if(type>=0)
		{
			int num = Byte.toUnsignedInt(b.getByte());
			int margin = Byte.toUnsignedInt(b.getByte());
			//System.out.println("margin "+margin);
			int num2 = b.getInt();
			pos = new Uv[num2];
			for (int i = 0; i < num2; i++)
			{
				Uv t = new Uv();
				t.id = b.getInt();
				t.id2 = b.getDouble();
				t.x = b.getInt();
				t.y = b.getInt();
				t.width = b.getInt();
				t.height = b.getInt();
				pos[i]=t;
				//System.out.printf("Found atlas %d, %f: (%d, %d) [%d, %d]\n", t.id, t.id2, t.x, t.y, t.width, t.height);
			}
			if (num == 1)
			{
				int width = b.getInt();
				int height = b.getInt();
				//todo: load texture
				try{
					File png=new File(s.externalResourcesDir, String.format(s.pxlsName + ".bytes.texture_%d.png", id));
					FileInputStream fis=new FileInputStream(png);
					image=ImageIO.read(fis);
					fis.close();
					System.out.println("read "+png.getName());
					if(image.getWidth()!=width||image.getHeight()!=height)
					{
						System.out.println(String.format("Wowie and we found an image need to be tracted %d,%d to %d,%d", image.getWidth(),image.getHeight(),width,height));
						BufferedImage rt=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
						((Graphics2D) rt.getGraphics()).setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_SPEED);
						((Graphics2D) rt.getGraphics()).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_SPEED);
						((Graphics2D) rt.getGraphics()).setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
						((Graphics2D) rt.getGraphics()).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
						((Graphics2D) rt.getGraphics()).setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
						((Graphics2D) rt.getGraphics()).setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
						rt.getGraphics().drawImage(image,0,0,width,height,null);
						image=rt;
					}
					if(s.shouldDelete)
						png.delete();
				}catch (Exception e)
				{
					e.printStackTrace();
					throw new IllegalArgumentException(e);
				}
			} else
			{
				NoelByteBuffer img = b.getSegment();
				try
				{
					image=ImageIO.read(new ByteArrayInputStream(img.getAllBytes()));

					/*
					FileOutputStream fos = new FileOutputStream("test"+id+".png");

					fos.write(img.getNBytes(img.size()));
					fos.close();*/
				} catch (Exception e)
				{
					e.printStackTrace();
					throw new IllegalArgumentException(e);
				}
			}
		}
	}
	@Deprecated
	static List<PxlImage> readFromBytes(NoelByteBuffer b,Settings s)
	{
		int type=b.getByte()-22;
		if(type>=0)
		{
			int num=Byte.toUnsignedInt(b.getByte());
			int margin=Byte.toUnsignedInt(b.getByte());
			int num2=b.getInt();
			Uv[] pos=new Uv[num2];
			for(int i=0;i<num2;i++)
			{
				Uv t=new Uv();
				t.id=b.getInt();
				t.id2=b.getDouble();
				t.x=b.getInt();
				t.y=b.getInt();
				t.width=b.getInt();
				t.height=b.getInt();
				System.out.printf("Found atlas %d, %f: (%d, %d) [%d, %d]\n",t.id,t.id2,t.x,t.y,t.width,t.height);
			}
			if(num==1)
			{
				int width=b.getInt();
				int height=b.getInt();
				//todo: load texture
			}
			else
			{
				NoelByteBuffer img=b.getSegment();
				try{
					FileOutputStream fos=new FileOutputStream("test.png");

					fos.write(img.getNBytes(img.size()));
					fos.close();
				}
				catch (Exception e)
				{

				}
			}
			//assign pxl image
			for(Uv u:pos)
			{

			}

		}
		return null;
	}
}
