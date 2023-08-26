package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

public class RectPackingAtlas
{
	int width,height;
	int[] heightMap;
	Sorts sorts;
	RectPackingAtlas(int width, int height)
	{
		this.width=width;
		this.height=height;
		heightMap=new int[width];
	}
	void resize(int w,int h)
	{
		Color[][] o=new Color[w][h];
		int[] oh=new int[w];
		System.arraycopy(heightMap,0,oh,0,width);
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
	Pair<Integer,Integer> putImage(int w,int h)
	{
		int place;
		while((place=findPlace(w,h))==-1)
			bigger();
		int x=place,y=heightMap[place];
		for(int i=0;i<w;i++)
			heightMap[x+i]=y+h;
		return new Pair<>(x,y);
	}
	long getUsed()
	{
		return Arrays.stream(heightMap).asLongStream().sum();
	}
	long getArea()
	{
		return (long) width * height;
	}
	static List<Pair<Integer,Integer>> getBestOne(List<Pair<Integer,Integer>> list,RectPackingAtlas[] op)
	{
		return Arrays.stream(Sorts.values()).map(
				s->{
					List<Pair<Integer,Pair<Integer,Integer>>> l=new ArrayList<>();
					for(int i=0;i<list.size();i++)
						l.add(new Pair<>(i,list.get(i)));
					l.sort((a,b)->{
						return s.func.compare(a.second,b.second);
					});
					List<Pair<Integer,Pair<Integer,Integer>>> rt=new ArrayList<>();
					RectPackingAtlas rectPackingAtlas=new RectPackingAtlas(1,1);
					rectPackingAtlas.sorts=s;
					for(Pair<Integer, Pair<Integer, Integer>> p:l)
						rt.add(new Pair<>(p.first,rectPackingAtlas.putImage(p.second.first,p.second.second)));
					rt.sort((a,b)->a.first-b.first);
					List<Pair<Integer,Integer>> toBeReturned=new ArrayList<>();
					for(int i=0;i<list.size();i++)
						if(rt.get(i).first==i)
							toBeReturned.add(rt.get(i).second);
						else
							throw new RuntimeException("check failed");
					return new Pair<>(rectPackingAtlas,toBeReturned);
				}
		).min((a,b)->{
			if(a.first.getArea()!=b.first.getArea())
				return Long.compare(a.first.getArea(),b.first.getArea());
			return Long.compare(a.first.getUsed(),b.first.getUsed());
		}).map(p->{
			op[0]=p.first;
			return p;
		}).get().second;
	}
	enum Sorts
	{
		WIDTH_POSITIVE((w,h)->w),
		WIDTH_NEGATIVE((w,h)->-w),
		HEIGHT_POSITIVE((w,h)->h),
		HEIGHT_NEGATIVE((w,h)->-h),
		AREA_POSITIVE((w,h)->w*h),
		AREA_NEGATIVE((w,h)->-w*h),
		;
		Comparator<Pair<Integer,Integer>> func;
		Sorts(BiFunction<Integer,Integer,Integer> func)
		{
			this.func= Comparator.comparingInt(a -> func.apply(a.first, a.second));
		}
	}
}
