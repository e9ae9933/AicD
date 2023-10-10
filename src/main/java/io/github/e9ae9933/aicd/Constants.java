package io.github.e9ae9933.aicd;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class Constants
{
	public static boolean shouldWeHandlePxls=true;
	public static String version="1.3";
	public static int versionCode=4;
	public static String versionUrl="https://aicd-1259776053.cos.ap-beijing.myqcloud.com/AicD-all.jar";

	public static Font unifont;

	static
	{
		try(InputStream is=Utils.readFromResources("unifont-15.0.06.otf",false))
		{
			unifont = Font.createFont(Font.TRUETYPE_FONT,is);
		} catch (Exception e)
		{
			e.printStackTrace();
			unifont=new Font(Font.SANS_SERIF, Font.PLAIN,0);
		}
	}
//	public static String[] versions={"0.0","1.0","1.1"};
}
