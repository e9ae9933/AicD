package io.github.e9ae9933.aicd.pxlskiller;

import com.google.gson.Gson;
import io.github.e9ae9933.aicd.Pair;
import io.github.e9ae9933.aicd.Policy;

import java.io.File;
import java.util.Map;

public class Settings
{
	Map<Pair<Integer,Double>,Pair<PxlImageAtlas, PxlImageAtlas.Uv>> idMap;
	Gson gson= Policy.gson;
	File externalResourcesDir;
	String pxlsName;
	boolean shouldDelete;
}
