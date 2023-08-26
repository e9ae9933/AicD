package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RectPacking
{
	int width,height;
	int[] heightMap;
	Color[][] map;
	RectPacking(int width,int height)
	{
		this.width=width;
		this.height=height;
		heightMap=new int[width];
		map=new Color[width][height];
	}
	void resize(int w,int h)
	{
		Color[][] o=new Color[w][h];
		for(int i=0;i<width;i++)
			System.arraycopy(map[i],0,o[i],0,heightMap[i]);
		int[] oh=new int[w];
		System.arraycopy(heightMap,0,oh,0,width);
		map=o;
		heightMap=oh;
		width=w;
		height=h;
	}
	void bigger()
	{
		if(width*2>height)
			resize(width,height*2);
		else resize(width*2,height);
	}
	int findPlace(int w,int h)
	{
		for(int i=width-w;i>=0;i--)
		{
			if((i==0||heightMap[i-1]>=heightMap[i]+h)&&heightMap[i]+h<=height)
				return i;
		}
		return -1;
	}
	Pair<Integer,Integer> putImage(BufferedImage image)
	{
		int w=image.getWidth();
		int h=image.getHeight();
		int place;
		while((place=findPlace(w,h))==-1)
			bigger();
		int x=place,y=heightMap[place];
		for(int i=0;i<w;i++)
			for(int j=0;j<h;j++)
				map[x+i][y+j]=new Color(image.getRGB(i,j),true);
		for(int i=0;i<w;i++)
			heightMap[x+i]=y+h;
		return new Pair<>(x,y);
	}
	BufferedImage getImage()
	{
		return getImage(new Color(0xFF00FF00,true));
	}
	BufferedImage getImage(Color def)
	{
		BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		for(int i=0;i<width;i++)
			for(int j=0;j<height;j++)
				image.setRGB(i,j,map[i][j]!=null?map[i][j].getRGB():def.getRGB());
		return image;
	}

}
