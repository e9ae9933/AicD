package io.github.e9ae9933.aicd.pxlskiller;

import com.google.gson.Gson;
import io.github.e9ae9933.aicd.Pair;
import io.github.e9ae9933.aicd.Policy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class Settings
{
	Map<Pair<Integer,Double>,Pair<PxlImageAtlas, PxlImageAtlas.Uv>> idMap=new LinkedHashMap<>();
	Map<Pair<Integer,Double>,PxlLayer> referenceMap=new LinkedHashMap<>();
	//List<PxlLayer> needReference=new ArrayList<>();
	List<Consumer<Settings>> tasksToBeDone=new ArrayList<>();
	void dealWithTasks()
	{
		tasksToBeDone.forEach(c->c.accept(this));
	}
	PxlCharacter target;

	Map<Pair<Integer,Double>, BufferedImage> idImage=new LinkedHashMap<>();


	Gson gson= Policy.gson;
	File externalResourcesDir;
	String pxlsName;
	boolean shouldDelete=false;
	boolean writeExtra=false;
	Random random=new Random();
}
