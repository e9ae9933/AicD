package io.github.e9ae9933.aicd.pxlskiller;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Pair;
import io.github.e9ae9933.aicd.Utils;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Rev
{
	public static void main(String[] args) throws Exception
	{
		/*
		System.out.println("Max memory "+Runtime.getRuntime().maxMemory()/1048576.0+" MB");
		Font font=null;
		InputStream is=Utils.readFromResources("unifont-15.0.06.otf",false);
		font=Font.createFont(Font.TRUETYPE_FONT,is);
		Settings s=new Settings();
		s.idImage=new LinkedHashMap<>();
		PxlCharacter chara=new PxlCharacter(new File("F:\\work\\master\\pxls\\noel"),s);
		ImageIO.write(chara.atlases[0].image,"png",new File("testimg.png"));
		FileOutputStream fos=new FileOutputStream("noel.pxls");
		fos.write(chara.outputAsBytes());
		fos.close();
		PxlCharacter character=new PxlCharacter(chara.output(new Settings()),new Settings());
		NoelByteBuffer.endAll();
		character.export(new File("export"),new Settings());*/

		args="--dir F:\\work\\master\\pxls --output F:\\test".split(" ");
		long time=System.currentTimeMillis();
		System.out.println("Process start with args "+args.length+" "+Arrays.toString(args));
		OptionParser optionParser=new OptionParser();
		ArgumentAcceptingOptionSpec<String> output=optionParser.accepts("output").withRequiredArg();
		ArgumentAcceptingOptionSpec<String> dir=optionParser.accepts("dir").withRequiredArg();
		//OptionSpec<Void> delete=optionParser.accepts("delete");
		OptionSet optionSet=optionParser.parse(args);
		File outputDir=new File(optionSet.valueOf(output)/*u*/);
		File pxlsDir=new File(optionSet.valueOf(dir));
		//boolean shouldDelete=optionSet.has(delete);
		if(outputDir.isDirectory()&&pxlsDir.isDirectory())
		{
			int n=Runtime.getRuntime().availableProcessors();
			ExecutorService service= Executors.newFixedThreadPool(n);
			System.out.println("Launch with "+n+" thread(s)");
			for(File dirDir:pxlsDir.listFiles())
			{
				if (!dirDir.isDirectory()) continue;
				service.execute(() ->
				{
					System.out.println((System.currentTimeMillis() - time) + " " + Thread.currentThread().getName() + " started with dir " + dirDir.getPath());
					try
					{
						Settings s=new Settings();
						//s.shouldDelete=shouldDelete;
						PxlCharacter chara=new PxlCharacter(dirDir,s);
						byte[] b=chara.outputAsBytes();
						FileOutputStream fos=new FileOutputStream(new File(outputDir,dirDir.getName()+".pxls"));
						fos.write(b);
						fos.close();
						System.out.println("finished "+dirDir);
					}
					catch (Exception e)
					{
						System.out.println("failed on "+dirDir);
						e.printStackTrace();
					}
				});
			}
			service.shutdown();
			while(!service.isTerminated())Thread.sleep(1);
			System.out.println("Service terminated");
			System.out.println("Time used "+(System.currentTimeMillis()-time)+" ms");
		}
		else throw new IllegalArgumentException("check your args");
	}
	static byte[] reverse(File dir)
	{
		Settings s=new Settings();
		s.idImage=new LinkedHashMap<>();
		PxlCharacter chara=new PxlCharacter(dir,s);
		return chara.outputAsBytes();
	}
	static BufferedImage getImage(List<BufferedImage> buf2,List<Pair<Integer,Integer>> points,boolean info,Font font)
	{
		List<BufferedImage> buf=new ArrayList<>(buf2);
		Consumer<RectPackingAtlas> consumer=(a)->{};
		if(info)
		{
			int w=800,h=300;
			BufferedImage infoImage=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
			Graphics2D g= (Graphics2D) infoImage.getGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			g.setFont(font.deriveFont(32f));
			g.setColor(Color.WHITE);
			g.fillRect(0,0,w,h);
			g.setColor(Color.BLACK);
			g.fillRect(8,8,w-16,h-16);
			g.setColor(Color.WHITE);
			g.drawString("Engine: AliceInCradle Toolbox",24,64);
			g.drawString(buf2.size()+" image(s)",24,96);
			buf.add(infoImage);
			consumer=(atlas)->{
				g.drawString("type "+atlas.sorts.name(),24,128);
				g.drawString(atlas.width+" * "+atlas.height,24,160);
				g.drawString("version "+System.getProperty("java.version"),24,192);
			};
		}
		RectPackingAtlas[] rt=new RectPackingAtlas[1];
		List<Pair<Integer,Integer>> list=RectPackingAtlas.getBestOne(buf.stream().map(i->new Pair<>(i.getWidth(),i.getHeight())).collect(Collectors.toList()),rt);
		consumer.accept(rt[0]);
		BufferedImage ans=make(rt[0],buf,list);
		points.clear();
		points.addAll(list);
		return ans;
	}
	static BufferedImage make(RectPackingAtlas atlas,List<BufferedImage> buf,List<Pair<Integer,Integer>> list)
	{
		if(buf.size()!=list.size())
			throw new IllegalArgumentException("false size");
		BufferedImage image=new BufferedImage(atlas.width,atlas.height,BufferedImage.TYPE_INT_ARGB);
		int n=buf.size();
		Graphics g=image.getGraphics();
		for(int i=0;i<n;i++)
			g.drawImage(buf.get(i),list.get(i).first,list.get(i).second,null);
		return image;
	}
}
