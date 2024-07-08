package io.github.e9ae9933.aicd.main;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.pxlskiller.PxlCharacter;
import io.github.e9ae9933.aicd.pxlskiller.Settings;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Specialized1
{
	public static void main(String[] args) throws Exception
	{
		OptionParser parser=new OptionParser();
		ArgumentAcceptingOptionSpec<String> pxls=parser.accepts("pxls","pxls file").withRequiredArg();
		ArgumentAcceptingOptionSpec<String> png=parser.accepts("png","png file").withRequiredArg();
		OptionSet set=(parser).parse(args);
		//set.g
		File pxlsFile=new File(set.valueOf(pxls));
		File pngFile=new File(set.valueOf(png));
		NoelByteBuffer buf=new NoelByteBuffer(Utils.readAllBytes(pxlsFile));
		Settings s=new Settings();
		PxlCharacter chara=new PxlCharacter(buf,s);
		BufferedImage img=chara.atlases[0].image;
		ImageIO.write(img,"png",pngFile);
	}
}
